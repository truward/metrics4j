package com.truward.metrics.json.internal.time;

/**
 * Represents a service for retrieving current time.
 *
 * @author Alexander Shabanov
 */
public interface TimeService {

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
