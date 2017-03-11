#!/usr/bin/env bash
sed 's/jobmanager.rpc.address: localhost/jobmanager.rpc.address: jobmanager/g' -i /opt/flink/conf/flink-conf.yaml
java -jar -Dspring.profiles.active=flink qb-server-0.1-SNAPSHOT.jar --flink.configDir=/opt/flink/conf --flink.jobIdHex=$(flink list --jobmanager=jobmanager:6123 | grep 'Queryable Billing Job' | cut -d ':' -f4 | awk '{$1=$1};1')