Metrics4J
=========

# What is it?

Metrics API is a set of libraries which are intended to capture structured metrics from
the application and record those metrics accordingly into the file.

## Sample usage

Add into your ```pom.xml``` dependencies on ``metrics4j-api`` and ``metrics4j-json-log``:

```xml
<dependency>
  <groupId>com.truward.metrics</groupId>
  <artifactId>metrics4j-api</artifactId>
  <version>${metrics4j.version}</version>
</dependency>
<dependency>
  <groupId>com.truward.metrics</groupId>
  <artifactId>metrics4j-api</artifactId>
  <version>${metrics4j.version}</version>
</dependency>
```

For ``metrics4j.version`` variable pick latest release version of metrics4j artifacts.
 
In initialization code (e.g. in DI container):

```java
// initialize metrics factory:
MetricsCreator metricsCreator = new JsonLogMetricsCreator(
  new File("log/metrics.json.log"));

// - or - initialize rolling metrics appender:
MetricsCreator metricsCreator = new JsonLogMetricsCreator(
  TimeBasedRollingLogSettings.newBuilder()
    .setFileNameBase("/tmp/my-app-metrics")
    .setCompressionType(CompressionType.GZIP)
    .setTimeDeltaMillis(60 * 60 * 1000L) // 1 hour, in milliseconds
    .build());
```

Then in data-processing code (try-with resources statement is used):
```java 
// metrics, around certain block, that needs to be metered
try (final Metrics metrics = metricsCreator.create()) {
  metrics.put(PredefinedMetricName.ORIGIN, "addTwoNumbers");
  
  // do operation
  final long startTime = System.currentTimeMillis(); 
  calculator.addTwoNumbers(numberA, numberB);
  final long timeDelta = System.currentTimeMillis() - startTime;
  
  // add time metric entries
  metrics.put(PredefinedMetricName.START_TIME, startTime);
  metrics.put(PredefinedMetricName.TIME_DELTA, timeDelta);  
}  // metrics object will be automatically closed and recorded
```

```java
// metrics object can be closed explicitly if, for some reason, you need to do it
final Metrics metrics = metricsCreator.create();
metrics.put(PredefinedMetricName.ORIGIN, "addTwoNumbers");

// do operation
final long startTime = System.currentTimeMillis(); 
calculator.addTwoNumbers(numberA, numberB);
final long timeDelta = System.currentTimeMillis() - startTime;
  
// add time metric entries
metrics.put(PredefinedMetricName.START_TIME, startTime);
metrics.put(PredefinedMetricName.TIME_DELTA, System.currentTimeMillis() - startTime);
  
metrics.close();
```