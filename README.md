Queryable Billing
=================

This is a prototype on how to build a robust billing system using Apache Flink's Queryable State feature.

The project consists of the following components

* **qb-job:** A queryable Flink Job with a Kafka source and file sink.
* **qb-server:** A small Spring Boot application, which is backed by the Flink's Queryable State and redirects request to the Flink job, such that the frontend clients do not have to deal with the the Queryable State Client directly.
* **qb-data-generator:** A data generator producing billable events to a Kafka Queue.
* **qb-frontend:** A small React/Redux application, which provides customers a web application to check there current monthly sub-total.

Build
-----
First of all, you need Flink, Kafka and Zookeeper containers. The latter two are on DockerHub. To build the Flink container run the following command in the *qb-flink* sub-folder
```
docker build . -t qb-flink
```
All of the above components run in their own Docker containers. To build the images run

**qb-job, qb-server, qb-data-generator**
```
./gradlew buildimage
```
**qb-frotend**

In *qb-frontend* sub-folder: 
```
node run build
docker build . -t qb-frontend
```

Run
---
```
docker-compose up -d
```

Verify
------
* Flink Dashboard 
    - <http://localhost:48081>   
* QB-Server
    - <http://localhost:8080/customers/{customer}> (e.g. *Emma* or *Noah*)
    - <http://localhost:8080/types/{type}> (*MESSAGE*, *DATA*, *CALL*, *PACK*, *MISC*)
* QB-Frontend    
    - <http://localhost:8088>
* File Output
    - `build/invoices/*` (Docker Volume mounted into the TaskManagers)


What's next
-----------

*Slides*
- [ ] Setup markdown in reveal.js 

*Backend*
- [x] Add 2nd taskmanager to docker setup @knaufk
- [x] Eigenes Flink Image
- [x] Data Generator should log what it's outputting (maybe sampled)
- [x] Increase event time speed in data generator, switch window size to 1 month
- [x] make data generator deterministic (should add up to fixed amount per month)
- [x] Improve invoice format
- [ ] Add more realistic event time skew and lateness
- [ ] which state is actually queried in case there is more than one window?
- [ ] Reactivate event types and include in Kafka serialization
- [ ] create Splunk Setup (Management Dashboard per type), see <http://blogs.splunk.com/2013/06/18/getting-data-from-your-rest-apis-into-splunk/> (alternatively investigate [simple-json-datasource](https://github.com/grafana/simple-json-datasource) Grafana plugin for management dashboard)
- [x] Checkpointing, test task manager failures
- [ ] JM HA, test job manager failures
- [ ] Simulate downstream system failures
- [ ] Expose metrics in job (for Flink Web UI)
- [ ] Investigate Corrupted State After Taskmanager Restart
- [ ] Test Data Generator Improvements (Different Amounts, Start Another Day)
- [ ] (optional) Change to real month


*Frontend*
- [x] Dockerize
- [x] Replace setInterval by setTimeout. Cleanup Scheduling of AJAX calls.
- [x] Colorful Failures/Success
- [x] Investigate why frontend is not displayed on Mac
- [x] Remove Links from Menu
- [x] Add month display
- [ ] Stabilize timing of ajax calls
- [ ] Replace hardCoded Backend-Address