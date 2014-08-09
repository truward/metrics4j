package com.truward.metrics.json;

import com.truward.metrics.Metrics;
import com.truward.metrics.PredefinedMetricNames;
import com.truward.metrics.reader.MetricsReader;
import com.truward.metrics.json.reader.StandardJsonMetricsReader;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link com.truward.metrics.json.JsonLogMetricsCreator}.
 *
 * @author Alexander Shabanov
 */
public final class JsonLogMetricsCreatorTest {
  private ByteArrayOutputStream os;
  private JsonLogMetricsCreator metricsCreator;

  @Before
  public void init() {
    os = new ByteArrayOutputStream(1000);

    // intentionally use buffered stream to check that each entry is flushed
    metricsCreator = new JsonLogMetricsCreator(new BufferedOutputStream(os));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldDisallowWritingToClosedMetrics() throws IOException {
    // Given
    final Metrics metrics = metricsCreator.create();
    metrics.put(PredefinedMetricNames.ORIGIN, "test");
    metrics.close();

    // When:
    metrics.put(PredefinedMetricNames.START_TIME, 1L);

    // Then: exception expected
  }

  @Test
  public void shouldDumpStandardValues() throws IOException {
    // Given:
    try (final Metrics metrics = metricsCreator.create()) {
      metrics.put(PredefinedMetricNames.ORIGIN, "test");
      metrics.put(PredefinedMetricNames.START_TIME, 1000L);
      metrics.put(PredefinedMetricNames.TIME_DELTA, 250L);
      metrics.put(PredefinedMetricNames.SUCCEEDED, true);
    }

    // When:
    metricsCreator.close();

    // Then:
    try (final MetricsReader reader = newMetricsReader()) {
      final Map<String, ?> map = reader.readNext();
      assertNotNull("should read metrics entry", map);
      assertEquals(4, map.size());
      assertEquals("test", map.get(PredefinedMetricNames.ORIGIN));
      assertEquals(true, map.get(PredefinedMetricNames.SUCCEEDED));
      assertEquals(1000, map.get(PredefinedMetricNames.START_TIME));
      assertEquals(250, map.get(PredefinedMetricNames.TIME_DELTA));

      assertNull("there should be no more metrics entries", reader.readNext());
    }
  }

  @Test
  public void shouldDumpNestedMap() throws IOException {
    // Given:
    try (final Metrics metrics = metricsCreator.create()) {
      metrics.put("nested", singletonMap("val", 1L));
    }

    // When:
    metricsCreator.close();

    // Then:
    try (final MetricsReader reader = newMetricsReader()) {
      final Map<String, ?> map = reader.readNext();
      assertNotNull("should read metrics entry", map);
      assertEquals(singletonMap("nested", singletonMap("val", 1)), map);
    }
  }

  @Test
  public void shouldDumpArray() throws IOException {
    // Given:
    try (final Metrics metrics = metricsCreator.create()) {
      metrics.put("array", Collections.singletonList("val"));
    }

    // When:
    metricsCreator.close();

    // Then:
    try (final MetricsReader reader = newMetricsReader()) {
      final Map<String, ?> map = reader.readNext();
      assertNotNull("should read metrics entry", map);
      assertEquals(singletonMap("array", singletonList("val")), map);
    }
  }

  @Test
  public void shouldDumpMultipleMetrics() throws IOException {
    // Given:
    try (final Metrics metrics = metricsCreator.create()) {
      metrics.put("val", "a");
      metrics.put("id", 1L);
    }
    try (final Metrics metrics = metricsCreator.create()) {
      metrics.put("val", "b");
    }

    // When:
    metricsCreator.close();

    // Then:
    try (final MetricsReader reader = newMetricsReader()) {
      final Map<String, ?> map1 = reader.readNext();
      assertNotNull("map1 is null", map1);
      assertEquals(2, map1.size());
      assertEquals(1, map1.get("id"));
      assertEquals("a", map1.get("val"));

      final Map<String, ?> map2 = reader.readNext();
      assertNotNull("map2 is null", map2);
      assertEquals(singletonMap("val", "b"), map2);

      assertNull("there should be no more metrics", reader.readNext());
    }
  }

  @Test
  public void shouldFlushEachRecord() throws IOException {
    for (int i = 0; i < 10; ++i) {
      // write metrics
      try (final Metrics metrics = metricsCreator.create()) {
        metrics.put("id", i);
      }

      // make sure newly introduced metric is in the stream without closing metrics creator
      try (final MetricsReader reader = newMetricsReader()) {
        for (int j = 0; j <= i; ++j) {
          final Map<String, ?> metrics = reader.readNext();
          assertNotNull(metrics);
          assertEquals(j, metrics.get("id"));
        }
      }
    }
  }

  //
  // Private
  //

  private MetricsReader newMetricsReader() {
    return new StandardJsonMetricsReader(new ByteArrayInputStream(os.toByteArray()));
  }
}
