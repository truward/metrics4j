package com.truward.metrics;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for {@link com.truward.metrics.NullMetricsCreator}.
 *
 * @author Alexander Shabanov
 */
public final class NullMetricsCreatorTest {
  private MetricsCreator metricsCreator;

  @Before
  public void init() {
    metricsCreator = new NullMetricsCreator();
  }

  @Test
  public void shouldCreateNullMetrics() {
    try (final Metrics metrics = metricsCreator.create()) {
      metrics.put("0", true);
      metrics.put("1", '1');
      metrics.put("2", 1);
      metrics.put("3", 1L);
      metrics.put("4", 1.0f);
      metrics.put("5", 1.0);
      metrics.put("6", "1");
      metrics.put("7", Arrays.asList(1, 2));
      metrics.put("8", Collections.singletonMap("a", 1));
    }
  }
}
