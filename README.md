Queryable Billing
=================

This is a prototype on how to build a robust billing system using Apache Flink's Queryable State feature.

The project consists of three components, the queryable Flink job (containing a Kafka source) and a Spring Boot application that can query the job for its internal state as well as a small jar that generates test data and produces to Kafka.

Manual Testing
--------------
1. Assemble all components:
    ```
    ./gradlew qb-job:shadowJar qb-server:bootRepackage qb-data-generator:shadowJar
    ```
2. Start demo:
    ```
    ./demo.sh
    ```
3. Query state (using curl or Postman):
    - <http://localhost:8080/customers/{customer}> (e.g. *Emma* or *Noah*)
    - <http://localhost:8080/types/{type}> (*MESSAGE*, *DATA*, *CALL*, *PACK*, *MISC*)
4. When finished, shut down everyhting:
    ```
    ./cleanup_after_demo.sh
    ```
    
### Server without Flink
Alternatively, the server can be started standalone without Flink as a backend: 
```
java -jar -Dspring.profiles.active=standalone qb-server/build/libs/qb-server-0.1-SNAPSHOT.jar 
```


What's next
-----------
- [x] Data generator jar (10 hardcoded names, runs to eternity, delay configurable)
- [x] create infrastructure for distributed tests (e.g. YARN via docker or vagrant, ...) -> script für cluster (1JM, 1TM , zookeeper, kafka, job, server, data generator)
- [ ] Reactivate event types and include in Kafka serialization
- [ ] create Splunk Setup (Management Dashboard per type), see <http://blogs.splunk.com/2013/06/18/getting-data-from-your-rest-apis-into-splunk/> (alternatively investigate [simple-json-datasource](https://github.com/grafana/simple-json-datasource) Grafana plugin for management dashboard)
- [ ] Checkpointing, test task manager failures
- [ ] JM HA, test job manager failures
- [ ] Simulate downstream system failures
- [ ] Expose metrics in job (for Flink Web UI)
- [ ] investigate kryo serialization error, when jobs runs for a while ?
