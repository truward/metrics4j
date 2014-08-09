package com.truward.metrics.json.internal.dumper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.truward.metrics.dumper.MapDumper;
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
 * Jackson-based map dumper.
 * UTF-8 encoding is used to write JSON values to the underlying output stream.
 * <p>THIS CLASS IS NOT A PART OF THE PUBLIC API.</p>
 *
 * @author Alexander Shabanov
 */
public class JacksonMapDumper implements MapDumper {
  private final OutputStream outputStream;
  private final JsonFactory factory = new JsonFactory();
  private final RecordCache recordCache;
  private final Object lock = new Object();
  private final Logger log = LoggerFactory.getLogger(getClass());

  public JacksonMapDumper(@Nonnull OutputStream outputStream, @Nonnull RecordCache recordCache) {
    this.factory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false); // do not automatically close output stream
    this.outputStream = outputStream;
    this.recordCache = recordCache;
  }

  @Override
  public void write(@Nonnull Map<String, Object> properties) {
    synchronized (lock) {
      try {
        // write json and close json generator
        try (final JsonGenerator generator = factory.createGenerator(outputStream)) {
          writeValue(generator, properties);
        }
        // write newline separator after written json entry, must be done after json generator is closed
        outputStream.write('\n');
      } catch (IOException e) {
        log.error("Error while writing map={}", properties, e);
      }
    }

    recordCache.take(properties);
  }

  @Override
  public void reportDuplicateEntry(@Nonnull Map<String, Object> source, @Nonnull String key) {
    if (!log.isErrorEnabled()) {
      return;
    }

    final Exception e = new Exception();
    e.fillInStackTrace(); // add stacktrace, so this error will be easily recognizable in the logs
    log.error("Duplicate entry with name={} in metrics={}", key, source, e);
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
