Metrics4J
=========

# What is it?

Metrics API is a set of libraries which are intended to capture structured metrics from
the application and record those metrics accordingly into the file.

## Sample usage

Add into your ```pom.xml``` dependencies on ``metrics4j-api`` and ``metrics4j-json-log``:

```xml
<dependency>
  <groupId>com.truward</groupId>
  <artifactId>metrics4j-api</artifactId>
  <version>${metrics4j.version}</version>
</dependency>
<dependency>
  <groupId>com.truward</groupId>
  <artifactId>metrics4j-api</artifactId>
  <version>${metrics4j.version}</version>
</dependency>
```

For ``metrics4j.version`` variable pick latest release version of metrics4j artifacts.
 
In java code:

```java
// initialize metrics factory
MetricsCreator metricsCreator = new JsonLogMetricsCreator(new File("log/metrics.json.log"));
 
// ...
// around certain block
try (final Metrics metrics = metricsCreator.create()) {
  metrics.put(PredefinedMetricName.ORIGIN, "addTwoNumbers");
  final long startTime = System.currentTimeMillis(); 
  metrics.put(PredefinedMetricName.START_TIME, startTime);
  
  // do operation
  calculator.addTwoNumbers(numberA, numberB);
  
  // add time delta metric entry
  metrics.put(PredefinedMetricName.TIME_DELTA, System.currentTimeMillis() - startTime);  
}  // metrics object will be automatically closed and recorded
```
