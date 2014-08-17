package com.truward.metrics.json;

import com.truward.metrics.Metrics;
import com.truward.metrics.MetricsCreator;
import com.truward.metrics.appender.MapAppender;
import com.truward.metrics.json.internal.appender.JacksonMapAppender;
import com.truward.metrics.json.internal.appender.RollingJacksonMapAppender;
import com.truward.metrics.json.internal.cache.EmptyRecordCache;
import com.truward.metrics.json.internal.cache.RecordCache;
import com.truward.metrics.json.settings.TimeBasedRollingLogSettings;
import com.truward.metrics.support.StandardMetrics;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Map;

/**
 * Metrics creator that dumps metrics as UTF-8 encoded JSON into the given output stream.
 *
 * @author Alexander Shabanov
 */
public class JsonLogMetricsCreator implements MetricsCreator, Closeable {

  private volatile MapAppender mapAppender;
  private RecordCache recordCache;

  public JsonLogMetricsCreator(@Nonnull OutputStream outputStream, @Nonnull RecordCache recordCache) {
    this.mapAppender = createMapDumper(outputStream, recordCache);
    this.recordCache = recordCache;
  }

  public JsonLogMetricsCreator(@Nonnull OutputStream outputStream) {
    this(outputStream, EmptyRecordCache.getInstance());
  }

  public JsonLogMetricsCreator(@Nonnull File file) throws FileNotFoundException {
    this(new BufferedOutputStream(new FileOutputStream(file, true), 4096));
  }

  public JsonLogMetricsCreator(@Nonnull String fileName) throws FileNotFoundException {
    this(new File(fileName));
  }

  public JsonLogMetricsCreator(@Nonnull TimeBasedRollingLogSettings settings, @Nonnull RecordCache recordCache) {
    this.mapAppender = new RollingJacksonMapAppender(settings, recordCache);
    this.recordCache = recordCache;
  }

  public JsonLogMetricsCreator(@Nonnull TimeBasedRollingLogSettings settings) {
    this(settings, EmptyRecordCache.getInstance());
  }

  @Nonnull
  @Override
  public Metrics create() {
    if (mapAppender == null) {
      throw new IllegalStateException("Can't create metric instance: output stream has been closed");
    }
    if (recordCache == null) {
      throw new IllegalStateException("Can't create metric instance: record cache has been discarded");
    }

    // reuse properties from the records cache
    final Map<String, Object> cachedProperties = recordCache.fetch();
    if (cachedProperties != null) {
      assert cachedProperties.isEmpty();
      return new StandardMetrics(cachedProperties, mapAppender);
    }

    return new StandardMetrics(mapAppender);
  }

  /**
   * Closes underlying output stream and writes nulls to all the internal fields.
   * No metrics should be written from any other thread when this object is closed.
   *
   * @throws IOException On I/O error when closing output stream
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException {
    final MapAppender appender = mapAppender;
    if (appender != null) {
      appender.close();
    }
    mapAppender = null;

    recordCache = null;
  }

  // Visible For Tests
  @Nonnull
  protected MapAppender createMapDumper(@Nonnull OutputStream outputStream, @Nonnull RecordCache recordCache) {
    return new JacksonMapAppender(outputStream, recordCache);
  }
}
