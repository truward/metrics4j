package com.truward.metrics.json.integration;

import com.truward.metrics.Metrics;
import com.truward.metrics.MetricsCreator;
import com.truward.metrics.PredefinedMetricNames;
import com.truward.metrics.json.JsonLogMetricsCreator;
import com.truward.metrics.json.reader.StandardJsonMetricsReader;
import com.truward.metrics.reader.MetricsReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Integration tests for append functionality.
 *
 * @author Alexander Shabanov
 */
@Ignore
public class JsonAppenderIntegrationTest {

  @Test
  public void shouldAppendWhenReopened() throws IOException {
    final File file = File.createTempFile("metrics4j", "appenderTest");

    // first write
    try (final JsonLogMetricsCreator metricsCreator = new JsonLogMetricsCreator(file.getAbsolutePath())) {
      try (final Metrics metrics = metricsCreator.create()) {
        metrics.put(PredefinedMetricNames.ORIGIN, "1");
      }
    }

    // second write
    try (final JsonLogMetricsCreator metricsCreator = new JsonLogMetricsCreator(file.getAbsolutePath())) {
      try (final Metrics metrics = metricsCreator.create()) {
        metrics.put(PredefinedMetricNames.ORIGIN, "2");
      }
    }

    try (final MetricsReader reader = new StandardJsonMetricsReader(new FileInputStream(file))) {
      Map<String, ?> metrics = reader.readNext();
      assertNotNull(metrics);
      assertEquals("1", metrics.get(PredefinedMetricNames.ORIGIN));

      metrics = reader.readNext();
      assertNotNull(metrics);
      assertEquals("2", metrics.get(PredefinedMetricNames.ORIGIN));

      assertNull(reader.readNext());
    }
  }
}
