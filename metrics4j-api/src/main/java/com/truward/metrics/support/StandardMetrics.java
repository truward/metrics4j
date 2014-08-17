package com.truward.metrics.support;

import com.truward.metrics.Metrics;
import com.truward.metrics.appender.MapAppender;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard, map-based implementation of {@link com.truward.metrics.Metrics}.
 *
 * @author Alexander Shabanov
 */
public class StandardMetrics implements Metrics {
  private MapAppender mapAppender;
  private Map<String, Object> properties;

  public StandardMetrics(@Nonnull Map<String, Object> properties, @Nonnull MapAppender mapAppender) {
    this.properties = properties;
    this.mapAppender = mapAppender;
  }

  public StandardMetrics(@Nonnull MapAppender mapAppender) {
    this(new HashMap<String, Object>(20), mapAppender);
  }

  @Override
  public final void put(@Nonnull String name, boolean value) {
    putEntry(name, value);
  }

  @Override
  public final void put(@Nonnull String name, char value) {
    putEntry(name, value);
  }

  @Override
  public final void put(@Nonnull String name, int value) {
    putEntry(name, value);
  }

  @Override
  public final void put(@Nonnull String name, float value) {
    putEntry(name, value);
  }

  @Override
  public final void put(@Nonnull String name, double value) {
    putEntry(name, value);
  }

  @Override
  public final void put(@Nonnull String name, long value) {
    putEntry(name, value);
  }

  @Override
  public final void put(@Nonnull String name, @Nonnull CharSequence value) {
    putEntry(name, value);
  }

  @Override
  public final <T> void put(@Nonnull String name, @Nonnull Collection<T> value) {
    putEntry(name, value);
  }

  @Override
  public final <K, V> void put(@Nonnull String name, @Nonnull Map<K, V> value) {
    putEntry(name, value);
  }

  @Override
  public void close() {
    final MapAppender appender = mapAppender;
    if (appender == null) {
      throw new IllegalStateException("Metrics instance has been already closed.");
    }
    mapAppender = null;
    appender.write(properties);
    properties = null;
  }

  //
  // Private
  //

  private void putEntry(String name, Object value) {
    if (name == null) {
      throw new IllegalArgumentException("name can't be null");
    }

    if (properties == null) {
      throw new IllegalStateException("Metric object is not writable, it has been closed");
    }

    if (properties.put(name, value) != null) {
      mapAppender.reportDuplicateEntry(properties, name);
    }
  }
}
