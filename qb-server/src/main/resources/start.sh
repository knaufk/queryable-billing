#!/usr/bin/env bash
sleep 15
java -jar -Dspring.profiles.active=flink qb-server-0.1-SNAPSHOT.jar --flink.configDir=/opt/flink/conf --flink.jobIdHex=$(flink list | grep 'Queryable Billing Job' | cut -d ':' -f4 | awk '{$1=$1};1')