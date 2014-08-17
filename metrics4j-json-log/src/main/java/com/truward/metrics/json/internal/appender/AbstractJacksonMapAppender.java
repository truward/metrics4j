package com.truward.metrics.json.internal.appender;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.truward.metrics.appender.MapAppender;
import com.truward.metrics.json.internal.cache.RecordCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
public abstract class AbstractJacksonMapAppender implements MapAppender {
  protected final JsonFactory factory = new JsonFactory();
  protected final RecordCache recordCache;
  protected final Object lock = new Object();
  protected final Logger log = LoggerFactory.getLogger(getClass());
  private volatile boolean closed = false;

  public AbstractJacksonMapAppender(@Nonnull RecordCache recordCache) {
    this.factory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false); // do not automatically close output stream
    this.recordCache = recordCache;
  }

  @Nonnull
  protected abstract OutputStream getOutputStream();

  protected void onWritePrepare() {
  }

  protected void onWriteStart() {
  }

  protected void onWriteEnd() {
  }

  protected abstract void onClose() throws IOException;

  @Override
  public final void write(@Nonnull Map<String, Object> properties) {
    onWritePrepare();
    synchronized (lock) {
      if (closed) {
        throw new IllegalStateException("Unable to write: object has been already closed");
      }

      onWriteStart();
      try {
        final OutputStream outputStream = getOutputStream();

        // write json and close json generator
        try (final JsonGenerator generator = factory.createGenerator(outputStream)) {
          writeValue(generator, properties);
        }
        // write newline separator after written json entry, must be done after json generator is closed
        outputStream.write('\n');
      } catch (IOException e) {
        log.error("Error while writing map={}", properties, e);
      } finally {
        onWriteEnd();
      }
    }

    recordCache.take(properties);
  }

  @Override
  public final void reportDuplicateEntry(@Nonnull Map<String, Object> source, @Nonnull String key) {
    if (!log.isErrorEnabled()) {
      return;
    }

    final Exception e = new Exception();
    e.fillInStackTrace(); // add stacktrace, so this error will be easily recognizable in the logs
    log.error("Duplicate entry with name={} in metrics={}", key, source, e);
  }

  @Override
  public final void close() throws IOException {
    synchronized (lock) {
      if (closed) {
        throw new IllegalStateException("Already closed");
      }

      onClose();
      closed = true;
    }
  }

  private static void writeMap(@Nonnull JsonGenerator jg, @Nonnull Map<String, Object> map) throws IOException {
    jg.writeStartObject();

    for (final Map.Entry<String, Object> entry : map.entrySet()) {
      final String key = entry.getKey();
      final Object value = entry.getValue();
      if (entry.getKey() == null) {
        throw new IllegalStateException("Metric key name is null");
      }

      if (value == null) {
        continue;
      }

      jg.writeFieldName(key);
      writeValue(jg, value);
    }

    jg.writeEndObject();
  }

  private static void writeValue(@Nonnull JsonGenerator jg, @Nonnull Object val) throws IOException {
    if (val instanceof String) {
      jg.writeString((String) val);
      return;
    }

    if (val instanceof Long) {
      jg.writeNumber((Long) val);
      return;
    }

    if (val instanceof Integer) {
      jg.writeNumber((Integer) val);
      return;
    }

    if (val instanceof Boolean) {
      jg.writeBoolean((Boolean) val);
      return;
    }

    if (val instanceof Short) {
      jg.writeNumber((Short) val);
      return;
    }

    if (val instanceof Float) {
      jg.writeNumber((Float) val);
      return;
    }

    if (val instanceof Double) {
      jg.writeNumber((Double) val);
      return;
    }

    if (val instanceof BigDecimal) {
      jg.writeNumber((BigDecimal) val);
      return;
    }

    final Class<?> valClass = val.getClass();

    if (Map.class.isAssignableFrom(valClass)) {
      // make sure keys are all strings
      final Map<?, ?> valMap = (Map<?, ?>) val;
      for (final Object keyObject : valMap.keySet()) {
        if (keyObject instanceof String) {
          continue;
        }
        throw new IllegalArgumentException("Map " + valMap + " contains non-string key");
      }

      //noinspection unchecked
      writeMap(jg, (Map<String, Object>) val);
      return;
    }

    if (List.class.isAssignableFrom(valClass)) {
      jg.writeStartArray();
      for (final Object value : (List<?>) val) {
        writeValue(jg, value);
      }
      jg.writeEndArray();
      return;
    }

    throw new UnsupportedOperationException("Unsupported value: " + val);
  }
}
