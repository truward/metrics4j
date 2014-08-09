package com.truward.metrics;

import javax.annotation.Nonnull;
import java.io.Closeable;

/**
 * A MetricsCreator object is used to create the related instances of {@link com.truward.metrics.Metrics} class.
 * MetricsCreator also encapsulates a way which is used to store and represent the newly created metrics.
 *
 * @author Alexander Shabanov
 * @see com.truward.metrics.Metrics
 */
public interface MetricsCreator {

  @Nonnull
  Metrics create();
}
