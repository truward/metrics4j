package com.truward.metrics;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a factory class that creates metrics object that logs nothing.
 * This object is useful for null safe code which should be able to work with non-loggable metrics.
 *
 * @author Alexander Shabanov
 */
public final class NullMetricsCreator implements MetricsCreator {

  /**
   * Static instance of the null metrics object. This instance is returned from
   * {@link NullMetricsCreator#create()} method.
   */
  public static final Metrics NULL_METRICS = new NullMetrics();

  /**
   * Returns special metrics instance that does nothing on closing.
   * See also {@link #NULL_METRICS}.
   *
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public Metrics create() {
    return NULL_METRICS;
  }

  //
  // Private
  //

  private static final class NullMetrics implements Metrics {

    @Override
    public void put(@Nonnull String name, boolean value) {
      // do nothing
    }

    @Override
    public void put(@Nonnull String name, char value) {
      // do nothing
    }

    @Override
    public void put(@Nonnull String name, int value) {
      // do nothing
    }

    @Override
    public void put(@Nonnull String name, float value) {
      // do nothing
    }

    @Override
    public void put(@Nonnull String name, double value) {
      // do nothing
    }

    @Override
    public void put(@Nonnull String name, long value) {
      // do nothing
    }

    @Override
    public void put(@Nonnull String name, @Nonnull CharSequence value) {
      // do nothing
    }

    @Override
    public <T> void put(@Nonnull String name, @Nonnull Collection<T> value) {
      // do nothing
    }

    @Override
    public <K, V> void put(@Nonnull String name, @Nonnull Map<K, V> value) {
      // do nothing
    }

    @Override
    public void close() {
      // do nothing
    }
  }
}
