package com.truward.metrics.json.settings;

import com.truward.metrics.time.TimeService;

import javax.annotation.Nonnull;

/**
 * Represents settings for rolling file metrics.
 *
 * @author Alexander Shabanov
 */
public final class TimeBasedRollingLogSettings {
  private final String fileNameBase;
  private final long timeDeltaMillis;
  private final CompressionType compressionType;
  private final String suffix;
  private final TimeService timeService;

  private TimeBasedRollingLogSettings(String fileNameBase, long timeDeltaMillis, CompressionType compressionType,
                                      String suffix, TimeService timeService) {
    if (fileNameBase == null) {
      throw new NullPointerException("fileNameBase can't be null");
    }

    if (timeDeltaMillis <= 0L) {
      throw new IllegalArgumentException("timeDelta can't be less or equal to zero");
    }

    if (compressionType == null) {
      throw new NullPointerException("compressionType can't be null");
    }

    if (suffix == null) {
      throw new NullPointerException("suffix can't be null");
    }

    if (timeService == null) {
      throw new NullPointerException("timeService can't be null");
    }

    this.fileNameBase = fileNameBase;
    this.timeDeltaMillis = timeDeltaMillis;
    this.compressionType = compressionType;
    this.suffix = suffix;
    this.timeService = timeService;
  }

  @Nonnull public String getFileNameBase() {
    return fileNameBase;
  }

  public long getTimeDeltaMillis() {
    return timeDeltaMillis;
  }

  @Nonnull public CompressionType getCompressionType() {
    return compressionType;
  }

  @Nonnull public String getSuffix() {
    return suffix;
  }

  @Nonnull public TimeService getTimeService() {
    return timeService;
  }

  //
  // Builder
  //

  @Nonnull
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Builder for the hosting class. */
  public static final class Builder {
    private String fileNameBase = "metrics-log-";
    private long timeDeltaMillis = 3600000L; // 1 hour
    private CompressionType compressionType = CompressionType.GZIP;
    private String suffix = ".log";
    private TimeService timeService = TimeService.DEFAULT;

    /** Hidden. */
    Builder() {
    }

    @Nonnull public Builder setFileNameBase(String value) {
      this.fileNameBase = value;
      return this;
    }

    @Nonnull public Builder setTimeDeltaMillis(long value) {
      this.timeDeltaMillis = value;
      return this;
    }

    @Nonnull public Builder setCompressionType(CompressionType value) {
      this.compressionType = value;
      return this;
    }

    @Nonnull public Builder setSuffix(String value) {
      this.suffix = value;
      return this;
    }

    @Nonnull public Builder setTimeService(TimeService value) {
      this.timeService = value;
      return this;
    }

    @Nonnull public TimeBasedRollingLogSettings build() {
      return new TimeBasedRollingLogSettings(fileNameBase, timeDeltaMillis, compressionType, suffix, timeService);
    }
  }
}
