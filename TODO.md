TODO List for metrics4j
=======================

## Features


### Reuse properties map allocated in StandardMetrics

Reuse properties map in StandardMetrics: make MapDumper pass HashMap back to MetricsCreator so that this class will
be able to reuse this map when another metrics instance will be created: it will help to minimize memory consumption
by Metrics instance as no new Map will be created and (likely) no HashMap reallocation will be made in the long 
running application.

