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
3. Assemble flink jar (now in this project's directory)
```
./gradlew qb-job:shadowJar 
```
4. Submit jar
```
$FLINK_DIR/build-target/bin/flink run qb-job/build/libs/qb-job-0.1-SNAPSHOT-all.jar
```
5. Build qb-server via
```
./gradlew qb-server:bootRepackage
```
6. Start via (replace job id)
```
java -jar qb-server/build/libs/qb-server-0.1-SNAPSHOT.jar --flink.configDir=$FLINK_DIR/flink-dist/src/main/resources --flink.jobIdHex=c9e2b987304fe3314b329fe0d17b2c8b
```
7. Query at <http://localhost:8080/query>

For faster feedback cycles have a look at `FlinkStateQueryServiceManualTest`

What's next
-----------
- [x] Output, i.e. bill creation (FileSink? maybe HDFS for exactly-once)
- [ ] **KKn** refine data structure & cleanup job 
- [ ] `TimestampSource` &rarr; Kafka source and Kafka test data generator
- [ ] add event-time handling (including late-arriving events)
- [ ] **KKn** Front end for queries (as a customer), e.g. [HAL Browser](http://docs.spring.io/spring-data/rest/docs/current/reference/html/#_the_hal_browser) or better: reactive app
- [ ] investigate [simple-json-datasource](https://github.com/grafana/simple-json-datasource) Grafana plugin for management dashboard
- [ ] investigate Splunk REST query for management dashboard
- [ ] **MB** create infrastructure for distributed tests (e.g. YARN via docker or vagrant, ...)
- [ ] Checkpointing, test task manager failures
- [ ] JM HA, test job manager failures
- [ ] Simulate downstream system failures

Nice to have:
- [ ] Get job id from flink instead of having to provide it at startup time
- [ ] improve REST interface, maybe using [Spring Data REST](http://docs.spring.io/spring-data/rest/docs/current/reference/html/)
- [ ] Expose metrics in job (for Flink Web UI or Grafana?)
