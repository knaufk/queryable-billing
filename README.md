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
./gradlew buildImage
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

