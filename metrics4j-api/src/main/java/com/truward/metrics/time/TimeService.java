package com.truward.metrics.time;

/**
 * Represents a service for retrieving current time.
 * Even though it is relatively simple to use {@link System#currentTimeMillis()}, this interface is used
 * in the metrics settings and metrics internals to improve testability.
 *
 * @author Alexander Shabanov
 */
public interface TimeService {

  /**
   * Default implementation of this service, delegates to {@link System#currentTimeMillis()}.
   */
  static TimeService DEFAULT = new TimeService() {
    @Override
    public long now() {
      return System.currentTimeMillis();
    }
  };

  /**
   * @return Current time, in milliseconds.
   */
  long now();
}
