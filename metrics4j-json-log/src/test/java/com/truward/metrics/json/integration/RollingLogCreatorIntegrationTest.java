package com.truward.metrics.json.integration;

import com.truward.metrics.Metrics;
import com.truward.metrics.PredefinedMetricNames;
import com.truward.metrics.json.JsonLogMetricsCreator;
import com.truward.metrics.json.internal.time.TimeService;
import com.truward.metrics.json.settings.CompressionType;
import com.truward.metrics.json.settings.TimeBasedRollingLogSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests creation of rolling logs.
 *
 * @author Alexander Shabanov
 */
@Ignore
public class RollingLogCreatorIntegrationTest {
  static final String BASE_NAME = "metrics4j-rolling-log";

  File dir;
  String basePath;


  @Before
  public void init() throws IOException {
    dir = new File("/tmp");
    basePath = dir.getAbsolutePath() + '/' + BASE_NAME;
  }

  @After
  public void clean() {
    final File[] logFiles = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(BASE_NAME);
      }
    });

    // delete all the log files
    for (final File file : logFiles) {
      if (!file.delete()) {
        System.err.println("Unable to delete " + file);
      }
    }
  }

  @Test
  public void shouldCreateRollingFiles() throws InterruptedException, IOException {
    final TestTimeService testTimeService = new TestTimeService(125000L);
    final JsonLogMetricsCreator creator = new JsonLogMetricsCreator(TimeBasedRollingLogSettings.newBuilder()
        .setFileNameBase(basePath)
        .setCompressionType(CompressionType.ZIP)
        .setTimeDeltaMillis(1000L) // 1 second
        .setSuffix(".log")
        .setTimeService(testTimeService)
        .build());

    for (int i = 0; i < 20; ++i) {
      try (final Metrics metrics = creator.create()) {
        metrics.put(PredefinedMetricNames.ORIGIN, "rollingMetric");
        metrics.put("pos", i);
      }
      testTimeService.sleep(250L); // there should be 4 entries in each compressed file
    }

    // make sure that 4 zip files has been created
    final File[] zipFiles = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(BASE_NAME) && name.endsWith("zip");
      }
    });
    assertEquals(4, zipFiles.length);

    creator.close();
  }

  @Test
  public void shouldCreateGzipRollingFiles() throws InterruptedException, IOException {
    final TestTimeService testTimeService = new TestTimeService(250000L);
    final JsonLogMetricsCreator creator = new JsonLogMetricsCreator(TimeBasedRollingLogSettings.newBuilder()
        .setFileNameBase(basePath)
        .setCompressionType(CompressionType.GZIP)
        .setTimeDeltaMillis(2000L) // 2 seconds
        .setSuffix(".log")
        .setTimeService(testTimeService)
        .build());

    long time = 0;
    for (int i = 0; i < 120; ++i) {
      try (final Metrics metrics = creator.create()) {
        metrics.put(PredefinedMetricNames.ORIGIN, "gzipMetric");
        metrics.put("pos", i);
        metrics.put("lastTime", time);
        time = System.currentTimeMillis();
        metrics.put("currentTime", time);
      }
      testTimeService.sleep(50L); // there should be 40 entries in each compressed file
    }

    // make sure that 4 zip files has been created
    final File[] zipFiles = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(BASE_NAME) && name.endsWith("gz");
      }
    });
    assertEquals(2, zipFiles.length);

    creator.close();
  }

  //
  // Private
  //

  static final class TestTimeService implements TimeService {
    volatile long currentTime;

    TestTimeService(long currentTime) {
      this.currentTime = currentTime;
    }

    void sleep(long millis) {
      currentTime += millis;
    }

    @Override
    public long now() {
      return currentTime;
    }
  }
}
