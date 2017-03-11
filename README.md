Queryable Billing
=================

This is a prototype on how to build a robust billing system using Apache Flink's Queryable State feature.

The project consists of three components, the queryable Flink job (containing a Kafka source) and a Spring Boot application that can query the job for its internal state as well as a small jar that generates test data and produces to Kafka.

Manual Testing
--------------
1. In the flink-contrib folder change the Flink Version in the Dockerfile to 1.2.0 and build the flink image tagging it flink:latest

2. Build all DockerImages
    ```
    ./gradlew buildImage
    ```
    and 
    `cd qb-frontend && npm build && docker build . -t qb-frontend`
3. Start demo:
    ```
    docker-compose up -d
    ```
4. Checkout Flink Dashboard <http://localhost:48081>    
5. Query Service
    - <http://localhost:8080/customers/{customer}> (e.g. *Emma* or *Noah*)
    - <http://localhost:8080/types/{type}> (*MESSAGE*, *DATA*, *CALL*, *PACK*, *MISC*)
6. Frontend
    - 
    
### Server without Flink
Alternatively, the server can be started standalone without Flink as a backend: 
```
java -jar -Dspring.profiles.active=standalone qb-server/build/libs/qb-server-0.1-SNAPSHOT.jar 
```


What's next
-----------

*Slides*
- [ ] Setup markdown in reveal.js 

*Backend*
- [x] Add 2nd taskmanager to docker setup @knaufk
- [ ] Eigenes Flink Image
- [ ] Improve invoice format
- [ ] Data Generator should log what it's outputting (maybe sampled)
- [ ] Increase event time speed in data generator, switch window size to 1 month
- [ ] which state is actually queried in case there is more than one window
- [ ] make data generator deterministic (should add up to fixed amount per month)
- [ ] Reactivate event types and include in Kafka serialization
- [ ] create Splunk Setup (Management Dashboard per type), see <http://blogs.splunk.com/2013/06/18/getting-data-from-your-rest-apis-into-splunk/> (alternatively investigate [simple-json-datasource](https://github.com/grafana/simple-json-datasource) Grafana plugin for management dashboard)
- [ ] Checkpointing, test task manager failures
- [ ] JM HA, test job manager failures
- [ ] Simulate downstream system failures
- [ ] Expose metrics in job (for Flink Web UI)

*Frontend*
- [x] Dockerize
- [x] Replace setInterval by setTimeout. Cleanup Scheduling of AJAX calls.
- [x] Colorful Failures/Success
- [ ] Remove Links from Menu
- [ ] Add month display
- [ ] Stabilize timing of ajax calls