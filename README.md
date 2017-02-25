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
4. Start local zookeeper and kafka, e.g.
    ```
    zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties
    ```
5. Submit jar
    ```
    $FLINK_DIR/build-target/bin/flink run -d qb-job/build/libs/qb-job-0.1-SNAPSHOT-all.jar --output /tmp
    ```
6. Build qb-server via
    ```
    ./gradlew qb-server:bootRepackage
    ```
7. Produce test data to kafka, i.e. lines like `1488142855,Charlie,199.99` to
    ```
    kafka-console-producer --broker-list localhost:9092 --topic billableevents.incoming
    ```
8. Start via (replace job id)
    ```
    java -jar -Dspring.profiles.active=flink qb-server/build/libs/qb-server-0.1-SNAPSHOT.jar --flink.configDir=$FLINK_DIR/flink-dist/src/main/resources --flink.jobIdHex=c9e2b987304fe3314b329fe0d17b2c8b
    ```
9. Alternatively, the server can be started standalone without Flink as a backend like this: 
    ```
    java -jar -Dspring.profiles.active=standalone qb-server/build/libs/qb-server-0.1-SNAPSHOT.jar 
    ```
10. Query at <http://localhost:8080/customers/{customer}> (You need to look in the text output for the names)
    Query at <http://localhost:8080/types/{type}> (MESSAGE, DATA, CALL, PACK, MISC)

For faster feedback cycles have a look at `FlinkStateQueryServiceManualTest`

What's next
-----------
- [ ] investigate why there is an additional source -> sink in job dashboard
- [ ] investigate kryo serialization error, when jobs runs for a while
- [ ] **MB** create infrastructure for distributed tests (e.g. YARN via docker or vagrant, ...)
- [ ] **KKn** create Splunk Setup
- [ ] `SimpleBillableEventSoure` &rarr; Kafka source and Kafka test data generator, add timestamp extractor
- [ ] investigate [simple-json-datasource](https://github.com/grafana/simple-json-datasource) Grafana plugin for management dashboard
- [x] investigate Splunk REST query for management dashboard -> Should work (Check: http://blogs.splunk.com/2013/06/18/getting-data-from-your-rest-apis-into-splunk/)
- [ ] Checkpointing, test task manager failures
- [ ] JM HA, test job manager failures
- [ ] Simulate downstream system failures

Nice to have:
- [ ] Get job id from flink instead of having to provide it at startup time
- [ ] improve REST interface, maybe using [Spring Data REST](http://docs.spring.io/spring-data/rest/docs/current/reference/html/)
- [ ] Expose metrics in job (for Flink Web UI or Grafana?)
