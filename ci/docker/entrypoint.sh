#!/bin/sh
umask 0000
/docker-entrypoint.sh & ${FLINK_HOME}/bin/flink run /opt/pravega-flink-connector-benchmark/pravega-flink-connector-benchmark.jar
