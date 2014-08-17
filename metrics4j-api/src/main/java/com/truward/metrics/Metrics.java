package com.truward.metrics;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.Collection;
import java.util.Map;

/**
 * Represents an entry in the metrics log.
 * Usually a metric is used to log time, spent to execute the particular operation.
 * <p/>
 * <p>The corresponding code might look as follows:</p>
 * <code>
 * try (final Metrics metrics = metricsCreator.create()) {
 *    metrics.put(ORIGIN, "OpenGLEngine.renderScene");
 *    final long startTime = System.currentTimeMillis();
 *    // execute operation...
 *    final long timeDelta = System.currentTimeMillis() - startTime;
 *    metrics.put(START_TIME, startTime);
 *    metrics.put(TIME_DELTA, timeDelta);
 * }
 * </code>
 *
 * @author Alexander Shabanov
 */
public interface Metrics extends Closeable {

  void put(@Nonnull String name, boolean value);

  void put(@Nonnull String name, char value);

  void put(@Nonnull String name, int value);

  void put(@Nonnull String name, float value);

  void put(@Nonnull String name, double value);

  void put(@Nonnull String name, long value);

  void put(@Nonnull String name, @Nonnull CharSequence value);

  <T> void put(@Nonnull String name, @Nonnull Collection<T> value);

  <K, V> void put(@Nonnull String name, @Nonnull Map<K, V> value);

  /**
   * Closes and writes metrics instance to the corresponding metrics log.
   * Subsequent calls to any of the put operations will result in an error.
   * <p/>
   * {@inheritDoc}
   */
  @Override
  void close();
}
