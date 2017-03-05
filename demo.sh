#!/usr/bin/env bash

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
YELLOW=$(tput setaf 3)
MAGENTA=$(tput setaf 5)
CYAN=$(tput setaf 6)
WHITE=$(tput setaf 7)
BOLD=$(tput bold)
REVERSE=$(tput rev)
RESET=$(tput sgr0)

LOG_DIR=logs
FLINK_DIR=/usr/local/Cellar/apache-flink/1.2.0/libexec
FLINK_BIN_DIR=${FLINK_DIR}/bin
FLINK_CONF_DIR=${FLINK_DIR}/conf
OUTPUT_DIR=/tmp/queryable-billing

set -eo pipefail

ask_for()
{
	declare question="$1" predefinedAnswer="$2"
	ANSWER=
	if [ -z "$predefinedAnswer" ] ; then
	    echo -e "$YELLOW $question:$RESET"
		read -p "> " -e ANSWER
		while [ -z "$ANSWER" ] ; do
			echo -e "$RED Please enter a value (or Ctrl-C to stop the script).$RESET"
			echo -e "$YELLOW $question:$RESET "
			read -p "> " -e ANSWER
		done
	else
	    echo -e "$YELLOW $question (default: $RESET$predefinedAnswer$YELLOW):$RESET"
		read -p "> " -e ANSWER
		if [ -z "$ANSWER" ] ; then
			ANSWER="$predefinedAnswer"
		fi
	fi
}

ask_forYN()
{
	declare question="$1" predefinedAnswer="$2"
	ANSWER=
	read -p "$YELLOW $question (default: $RESET$predefinedAnswer$YELLOW) $RESET" -e ANSWER
	if [ -z "$ANSWER" ] ; then
		ANSWER="$predefinedAnswer"
	fi
	ANSWER=$(echo "$ANSWER" | tr '[:upper:]' '[:lower:]')
	while [ "$ANSWER" != "y"  -a "$ANSWER" != "n"  ] ; do
		echo "$RED Please enter either Y or N for $YELLOW$question$RED or stop the script with Ctrl-C.$RESET"
		read -p "$YELLOW $question (default: $RESET$predefinedAnswer$YELLOW) $RESET" -e ANSWER
		if [ -z "$ANSWER" ] ; then
			ANSWER="$predefinedAnswer"
		fi
		ANSWER=$(echo "$ANSWER" | tr '[:upper:]' '[:lower:]')
	done
}

run_and_store_result()
{
	declare description=$1
	shift

    tput el # delete whole line
	echo -en "$description..."
	set +e
	RESULT=$($*)
	declare RETURN_VALUE=$?
	tput hpa 40 # go to 30'th column
	tput el # delete rest of line
	if [ ${RETURN_VALUE} -eq 0 ] ; then
		echo "$GREEN [ SUCCESS ]$RESET"
		SUCCEEDED=true
	else
		echo "$RED [ FAILED  ]$RESET"
		SUCCEEDED=false
	fi
	set -e
}

run_detached()
{
	declare description=$1
	shift

	declare log_file=$1
	shift

    tput el # delete whole line
	echo -en "$description..."
	set +e

	$* </dev/null &>> ${log_file} &
}

success()
{
    tput hpa 40 # go to 30'th column
	tput el # delete rest of line
    echo "$GREEN [ SUCCESS ]$RESET"
	set -e
}


echo "============================"
echo "    Setup ${CYAN}Flink${RESET}"
echo "============================"
run_and_store_result "Start Flink cluster (1 JM & 1 TM)" ${FLINK_BIN_DIR}/start-local.sh
open -g "http://localhost:8081"

echo "============================"
echo "    Setup ${CYAN}ZooKeeper${RESET}"
echo "============================"
run_detached "Start ZooKeeper cluster (1 node)" ${LOG_DIR}/zookeeper.out zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties
success

echo "============================"
echo "    Setup ${CYAN}Kafka${RESET}"
echo "============================"
run_detached "Start Kafka cluster (1 broker)" ${LOG_DIR}/kafka.out kafka-server-start /usr/local/etc/kafka/server.properties
success

echo "============================"
echo "    Start ${CYAN}Job${RESET}"
echo "============================"
ask_for "Output directory for final invoices" ${OUTPUT_DIR}
OUTPUT_DIR="$ANSWER"
run_detached "Start Flink job" ${LOG_DIR}/qb-job.out ${FLINK_BIN_DIR}/flink run -d qb-job/build/libs/qb-job-0.1-SNAPSHOT-all.jar --output ${OUTPUT_DIR} --bootstrap-servers localhost:9092
sleep 10
success

echo "Getting job id..."
job_id=$(curl -s -L --connect-timeout 5 "localhost:8081/joboverview" |
    python -c 'import json,sys; print "\n".join([j["jid"] for j in json.load(sys.stdin)["running"] if j["name"]=="'Queryable\ Billing\ Job'"])' 2>/dev/null)
echo "  Job id: $MAGENTA $job_id $RESET"

echo "============================"
echo "    Start ${CYAN}Server${RESET}"
echo "============================"

run_detached "Start server" ${LOG_DIR}/qb-server.out java -jar -Dspring.profiles.active=flink qb-server/build/libs/qb-server-0.1-SNAPSHOT.jar --flink.configDir=${FLINK_CONF_DIR} --flink.jobIdHex=${job_id}
success

echo "============================"
echo "    Start ${CYAN}Data Generator${RESET}"
echo "============================"
run_detached "Start Test Data Generator" ${LOG_DIR}/qb-data-generator.out java -jar qb-data-generator/build/libs/qb-data-generator-0.1-SNAPSHOT-all.jar
success

