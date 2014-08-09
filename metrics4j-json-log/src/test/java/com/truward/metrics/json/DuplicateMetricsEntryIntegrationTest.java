package com.truward.metrics.json;

import com.truward.metrics.Metrics;
import com.truward.metrics.PredefinedMetricNames;
import com.truward.metrics.reader.MetricsReader;
import com.truward.metrics.json.reader.StandardJsonMetricsReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

/**
 * Tests that duplicate error appeared in the logs.
 * Test ignored as it is supposed to be run by developer to see duplicate entry error in the logs.
 *
 * @author Alexander Shabanov
 */
@Ignore
public final class DuplicateMetricsEntryIntegrationTest {

  private final Logger mockLog = mock(Logger.class);
  private JsonLogMetricsCreator creator;
  private ByteArrayOutputStream os;

  @Before
  public void init() {
    reset(mockLog);

    os = new ByteArrayOutputStream();
    creator = new JsonLogMetricsCreator(os);
  }

  @After
  public void close() throws IOException {
    creator.close();
  }

  @Test
  public void shouldRecordDuplicateEntry() throws IOException {
    // Given:
    final String newOriginName = "newOriginName";

    // When:
    try (final Metrics metrics = creator.create()) {
      metrics.put(PredefinedMetricNames.ORIGIN, "oldOrigin");
      metrics.put(PredefinedMetricNames.START_TIME, 1);
      metrics.put(PredefinedMetricNames.ORIGIN, newOriginName); // should result in an error
    }

    // Then:
    try (final MetricsReader metricsReader = new StandardJsonMetricsReader(new ByteArrayInputStream(os.toByteArray()))) {
      final Map<String, ?> metrics = metricsReader.readNext();
      assertNotNull("metrics", metrics);
      assertEquals(newOriginName, metrics.get(PredefinedMetricNames.ORIGIN));
    }
  }
}
