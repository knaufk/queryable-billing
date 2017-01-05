Queryable Billing
=================

This is a prototype on how to build a robust billing system using Apache Flink's Queryable State feature.

The project consists of two components, the queryable Flink job and a Spring Boot application that can query the job for its internal state.

Manual Testing
--------------
1. Check out [Apache Flink](https://github.com/apache/flink)'s master branch and build using maven, i.e. go to flink repo (from now on $FLINK_DIR) and
```
mvn clean package -DskipTests
```
2. Start local flink
```
$FLINK_DIR/build-target/bin/start-local.sh
```
3. Assemble flink jar (now in project dir)
```
./gradlew qb-job:shadowJar 
```
4. Submit jar
```
$FLINK_DIR/build-target/bin/flink run qb-job/build/libs/qb-job-0.1-SNAPSHOT-all.jar
```
5. Copy job id to `FlinkStateQueryServiceManualTest` and change flink config dir to `$FLINK_DIR/flink-dist/src/main/resources`. Then run it.