How to release
==============

## Operation Sequence

### Set version

``
mvn versions:set -DnewVersion=1.0.3 -P release
``

### Perform release

``
mvn clean deploy -P release
``


## Miscellaneous

### Prepare the release

``
mvn release:prepare -Prelease
``

See release plugin info on apache.maven.org

### Deploy

See intructions at http://central.sonatype.org/pages/apache-maven.html


