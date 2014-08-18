Metrics4J
=========

# What is it?

Metrics API is a library which is intended to be used to capture 
structured metrics from the application and record those metrics accordingly into the file.

This task is very similar to logging with the exception that the sole intent of
metrics is to record structured files that are ready to be analyzed by specific tools.

There are no strict requirements on the structure of metrics, but the closest data structure
that might represent a metric is a string-to-object map. Each object value in that map can be:

* A null value
* A primitive type, i.e. ``byte``, ``char``, ``short``, ``int``, ``long``, ``float``, 
  ``double`` or ``boolean``
* A sequence of characters - ``java.lang.CharSequence``
* A collection of objects, where each object should be of any type specified in this list.
* Another string-to-object map, where each object should be of any type specified in this list. 

The resultant file with metrics record might be formatted as JSON. Its structure might look as follows:

```json
{"origin": "UserService.loginUser", "startTime": 1231000000, "timeDelta": 24}
{"origin": "UserService.registerUser", "succeeded": true, "startTime": 1231500000, "timeDelta": 235}
{"origin": "UserService.deleteUser", "startTime": 1235000000, "timeDelta": 51}
{"origin": "BankingService.transferMoney", "amount": 400, "currency": "USD", "startTime": 1245000000, "timeDelta": 109}
```

There are certain predefined value names which SHOULD be associated with the corresponding
values in the metrics structure. 
Such names and corresponding types are defined in ``com.truward.metrics.PredefinedMetricNames``:

* ``origin``, associated type: ``String``. 
  This entry SHOULD specify origin of the particular metric record. It might be a method name, 
  like ``UserService.registerUser`` or a name of an operation, performed in the particular 
  method, like ``UserService.registerUser.checkEmail``, or, say, REST API invocation signature,
  like ``POST /user/register``.
* ``startTime``, associated type: ``long``. Units: milliseconds.
  This entry SHOULD specify time in milliseconds when the operation started. The simplest way
  to get this value is to invoke ``System.currentTimeMillis()`` and record the returned value under
  this name.
* ``timeDelta``, associated type: ``long``. Units: milliseconds.
  This entry SHOULD specify time in milliseconds which should indicate how long the corresponding
  operation took.
* ``succeeded``, associated type: ``boolean``.
  This entry SHOULD indicate whether the corresponding operation completed successfully or not.

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
 
In initialization code (e.g. somewhere in dependency injection container configuration):

```java
// initialize metrics factory, that will record metrics into the file as JSON records.
MetricsCreator metricsCreator = new JsonLogMetricsCreator("log/metrics.json.log");
  
// alternatively File object might be passed to the class constructor with the same
// effect:
MetricsCreator metricsCreator = new JsonLogMetricsCreator(
  new File("log/metrics.json.log", true));
```

Also metrics creator might be initialized with time-based rolling file settings. This
will make metrics creator automatically archive logs after certain time is passed and
start a new log file:

```java
// initialize metrics factory, that will record metrics into the rolling files as JSON records.
// after ``timeDeltaMillis`` msec log file will be archived by using the specified compression engine
// and new log file will be opened.
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