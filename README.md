Queryable Billing
=================

This is a prototype on how to build a robust billing system using Apache Flink's Queryable State feature.

The project consists of the following components

* **qb-job:** A queryable Flink Job with a Kafka source and file sink.
* **qb-server:** A small Spring Boot application, which is backed by the Flink's Queryable State and redirects request to the Flink job, such that the frontend clients do not have to deal with the the Queryable State Client directly.
* **qb-data-generator:** A data generator producing billable events to a Kafka Queue.
* **qb-frontend:** A small React/Redux application, which provides customers a web application to check there current monthly sub-total.

[![Build Status](https://travis-ci.org/mbode/queryable-billing.svg?branch=master)](https://travis-ci.org/mbode/queryable-billing)

Talk
----
### Upcoming
- [Berlin Buzzwords 2017](https://berlinbuzzwords.de/17/session/queryable-state-or-how-build-billing-system-without-database)

### Past
- [Flink Forward San Francisco](http://sf.flink-forward.org/kb_sessions/queryable-state-or-how-to-build-a-billing-system-without-a-database/) \[ [YouTube](https://www.youtube.com/watch?v=cZbnNzKSBb0) | [Slides](http://sf.flink-forward.org/wp-content/uploads/2017/02/Flink-Forward-SF-2017_Konstantin-Knauf_Maximilian-Bode_Queryable-State-Or-How-To-Build-A-Billing-System-WIthout-A-Database.pdf) \]
- [Apache Flink Meetup Munich](https://www.meetup.com/de-DE/Apache-Flink-Meetup-Munich/events/237883833/) / [Big Data Stream Analytics](https://www.meetup.com/de-DE/Big-Data-Stream-Analytics/events/238081597/)

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
npm install
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

