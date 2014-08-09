package com.truward.metrics;

/**
 * Predefined names of metrics entries.
 *
 * @author Alexander Shabanov
 */
public final class PredefinedMetricNames {
  /** Hidden */
  private PredefinedMetricNames() {
  }

  /**
   * Predefined entry name.
   * <p/>
   * Identifies an origin of the given metrics entry, e.g. method signature or REST API path.
   * The associated value expected to be a string.
   */
  public static final String ORIGIN = "origin";

  /**
   * Predefined entry name.
   * <p/>
   * Start time of the corresponding operation.
   * The associated value expected to be a long number, that represents time in milliseconds between
   * the current time and midnight, January 1, 1970 UTC.
   *
   * @see System#currentTimeMillis()
   * @see #TIME_DELTA
   */
  public static final String START_TIME = "startTime";

  /**
   * Predefined entry name.
   * <p/>
   * Time in that was spent to execute the corresponding operation.
   * The associated value expected to be a long number, that represents time in milliseconds.
   *
   * @see #START_TIME
   */
  public static final String TIME_DELTA = "timeDelta";

  /**
   * Predefined entry name.
   * <p/>
   * Identifies the result of the operation, i.e. whether the operation succeeded or failed.
   * The associated value expected to be of boolean type.
   */
  public static final String SUCCEEDED = "succeeded";
}
