package com.truward.metrics.json;

import com.truward.metrics.Metrics;
import com.truward.metrics.PredefinedMetricNames;
import com.truward.metrics.reader.MetricsReader;
import com.truward.metrics.json.reader.StandardJsonMetricsReader;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link com.truward.metrics.json.reader.AbstractJsonMetricsReader}.
 *
 * @author Alexander Shabanov
 */
public final class StandardJsonMetricsReaderTest {
  private ByteArrayOutputStream os;
  private JsonLogMetricsCreator metricsCreator;

  @Before
  public void init() {
    os = new ByteArrayOutputStream(1000);
    metricsCreator = new JsonLogMetricsCreator(os);
  }

  @Test
  public void shouldWriteAndReadMetrics() throws IOException {
    // Given:
    final int entriesCount = 100;
    for (int i = 0; i < entriesCount; ++i) {
      writeMetricsRecord(i);
    }

    // When:
    metricsCreator.close();

    // Then:
    assertMetricsRead(entriesCount, 100, 100);
    assertMetricsRead(entriesCount, 10, 100);
    assertMetricsRead(entriesCount, 11, 107);
    assertMetricsRead(entriesCount, 9, 101);
    assertMetricsRead(entriesCount, 1, 100);
  }

  @Test
  public void shouldReadNestedObjects() throws IOException {
    // Given:
    final String origin = "test";
    final List<Object> parameters = Arrays.asList(1, "str", Long.MAX_VALUE, Arrays.asList(3));
    final Map<String, Object> traits = Collections.<String, Object>singletonMap("a", "b");

    // When:
    try (final Metrics metrics = metricsCreator.create()) {
      metrics.put(PredefinedMetricNames.ORIGIN, origin);
      metrics.put("parameters", parameters);
      metrics.put("traits", traits);
    }

    // Then:
    try (final MetricsReader metricsReader = newMetricsReader(100, 100)) {
      final Map<String, ?> metrics = metricsReader.readNext();
      assertNotNull("Should have non-null metrics", metrics);
      assertEquals(origin, metrics.get(PredefinedMetricNames.ORIGIN));
      assertEquals(parameters, metrics.get("parameters"));
      assertEquals(traits, metrics.get("traits"));

      assertNull("Should have null metrics", metricsReader.readNext());
    }
  }

  //
  // Private
  //

  private MetricsReader newMetricsReader(int initialBufferSize, int maxBufferSize) {
    return new StandardJsonMetricsReader(new ByteArrayInputStream(os.toByteArray()), initialBufferSize, maxBufferSize);
  }

  private void writeMetricsRecord(int id) {
    try (final Metrics metrics = metricsCreator.create()) {
      metrics.put(PredefinedMetricNames.ORIGIN, "test");
      metrics.put(PredefinedMetricNames.START_TIME, 1000);
      metrics.put(PredefinedMetricNames.SUCCEEDED, true);
      metrics.put("id", id);
    }
  }

  private void assertMetricsRead(int expectedCount, int initialBufferSize, int maxBufferSize) throws IOException {
    try (final MetricsReader reader = newMetricsReader(initialBufferSize, maxBufferSize)) {
      for (int i = 0; i < expectedCount; ++i) {
        final Map<String, ?> metrics = reader.readNext();
        assertNotNull("Entry #" + i + " not found for initialBufferSize=" + initialBufferSize +
            ", maxBufferSize=" + maxBufferSize, metrics);

        assertEquals(4, metrics.size());
        assertEquals(i, metrics.get("id"));
        assertEquals("test", metrics.get(PredefinedMetricNames.ORIGIN));
        assertEquals(1000, metrics.get(PredefinedMetricNames.START_TIME));
        assertEquals(true, metrics.get(PredefinedMetricNames.SUCCEEDED));
      }

      assertNull("There should be no more metrics", reader.readNext());
    }
  }
}
