#!/usr/bin/env bash

pkill -f qb-data-generator
pkill -f qb-server
kafka-server-stop
zookeeper-server-stop
/usr/local/Cellar/apache-flink/1.2.0/libexec/bin/jobmanager.sh stop-all