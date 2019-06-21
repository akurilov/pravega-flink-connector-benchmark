#!/bin/sh
umask 0000
${FLINK_HOME}/bin/start-cluster.sh & ${FLINK_HOME}/bin/flink run /opt/pravega-flink-connector-benchmark/pravega-flink-connector-benchmark.jar
