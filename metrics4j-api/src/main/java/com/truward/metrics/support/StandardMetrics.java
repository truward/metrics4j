package com.truward.metrics.support;

import com.truward.metrics.Metrics;
import com.truward.metrics.dumper.MapDumper;

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
  private MapDumper mapDumper;
  private Map<String, Object> properties;

  public StandardMetrics(@Nonnull Map<String, Object> properties, @Nonnull MapDumper mapDumper) {
    this.properties = properties;
    this.mapDumper = mapDumper;
  }

  public StandardMetrics(@Nonnull MapDumper mapDumper) {
    this(new HashMap<String, Object>(20), mapDumper);
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
    final MapDumper dumper = mapDumper;
    if (dumper == null) {
      throw new IllegalStateException("Metrics instance has been already closed.");
    }
    mapDumper = null;
    dumper.write(properties);
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
      mapDumper.reportDuplicateEntry(properties, name);
    }
  }
}
