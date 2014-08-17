package com.truward.metrics.json.integration;

import com.truward.metrics.Metrics;
import com.truward.metrics.PredefinedMetricNames;
import com.truward.metrics.json.JsonLogMetricsCreator;
import com.truward.metrics.reader.MetricsReader;
import com.truward.metrics.json.reader.StandardJsonMetricsReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Multithread integration tests for {@link com.truward.metrics.json.JsonLogMetricsCreator}.
 *
 * @author Alexander Shabanov
 */
@Ignore
public final class ConcurrentJsonLogCreatorIntegrationTest {
  private JsonLogMetricsCreator metricsCreator;
  private ThreadPoolExecutor executor;
  private File file;

  @Before
  public void init() throws IOException {
    file = File.createTempFile("metrics4j", "concurrentTest");
    metricsCreator = new JsonLogMetricsCreator(file);
    executor = new ThreadPoolExecutor(10, 100, 1L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
  }

  @After
  public void close() throws IOException {
    metricsCreator.close();
    executor.shutdown();
  }

  @Test
  public void shouldWriteMultipleValues() throws Exception {
    // Given:
    final int entryCount = 100;
    final List<Future<Void>> tasks = new ArrayList<>(entryCount);

    // When:
    for (int i = 0; i < entryCount; ++i) {
      final int id = i;
      tasks.add(executor.submit(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          try (final Metrics metrics = metricsCreator.create()) {
            metrics.put(PredefinedMetricNames.ORIGIN, Integer.toString(id));
            metrics.put(PredefinedMetricNames.START_TIME, System.currentTimeMillis());
            metrics.put(PredefinedMetricNames.TIME_DELTA, 10L);
            metrics.put(PredefinedMetricNames.SUCCEEDED, true);
          }

          return null;
        }
      }));
    }

    for (int i = 0; i < entryCount; ++i) {
      assertNull(tasks.get(i).get()); // execute and wait for complete
    }

    // Then:
    final int[] ids = new int[entryCount];

    // read entry ids
    try (final MetricsReader reader = new StandardJsonMetricsReader(new FileInputStream(file))) {
      for (int i = 0; i < entryCount; ++i) {
        final Map<String, ?> entry = reader.readNext();
        assertNotNull("Entry #" + i + " not found", entry);
        assertEquals(4, entry.size());
        final int id = Integer.parseInt(String.valueOf(entry.get(PredefinedMetricNames.ORIGIN)));
        ids[i] = id;
      }

      assertNull("There should be no more entries", reader.readNext());
    }

    // make sure all origins have been recorded and then read properly
    Arrays.sort(ids);
    for (int i = 0; i < entryCount; ++i) {
      assertEquals(i, ids[i]);
    }
  }
}
