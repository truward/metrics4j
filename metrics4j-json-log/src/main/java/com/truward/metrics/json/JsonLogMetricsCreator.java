package com.truward.metrics.json;

import com.truward.metrics.Metrics;
import com.truward.metrics.MetricsCreator;
import com.truward.metrics.dumper.MapDumper;
import com.truward.metrics.json.internal.cache.EmptyRecordCache;
import com.truward.metrics.json.internal.cache.RecordCache;
import com.truward.metrics.json.internal.dumper.JacksonMapDumper;
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

  private MapDumper mapDumper;
  private OutputStream outputStream;
  private RecordCache recordCache;

  public JsonLogMetricsCreator(@Nonnull OutputStream outputStream, @Nonnull RecordCache recordCache) {
    this.outputStream = outputStream;
    this.mapDumper = createMapDumper(outputStream, recordCache);
    this.recordCache = recordCache;
  }

  public JsonLogMetricsCreator(@Nonnull OutputStream outputStream) {
    this(outputStream, EmptyRecordCache.getInstance());
  }

  public JsonLogMetricsCreator(@Nonnull File file) throws FileNotFoundException {
    this(new BufferedOutputStream(new FileOutputStream(file, true), 4096));
  }

  @Nonnull
  @Override
  public Metrics create() {
    if (mapDumper == null) {
      throw new IllegalStateException("Can't create metric instance: output stream has been closed");
    }
    if (recordCache == null) {
      throw new IllegalStateException("Can't create metric instance: record cache has been discarded");
    }

    // reuse properties from the records cache
    final Map<String, Object> cachedProperties = recordCache.fetch();
    if (cachedProperties != null) {
      assert cachedProperties.isEmpty();
      return new StandardMetrics(cachedProperties, mapDumper);
    }

    return new StandardMetrics(mapDumper);
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
    mapDumper = null;
    recordCache = null;

    if (outputStream != null) {
      outputStream.close();
      outputStream = null;
    }
  }

  // Visible For Tests
  @Nonnull
  protected MapDumper createMapDumper(@Nonnull OutputStream outputStream, @Nonnull RecordCache recordCache) {
    return new JacksonMapDumper(outputStream, recordCache);
  }
}
