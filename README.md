# pravega-flink-connector-benchmark
Pravega Flink Connector Benchmark

# Build

```bash
./gradlew connectorJar
./gradlew jar
```

## Build With Specific Pravega Version

```bash
./gradlew -PpravegaVersion=0.6.0-50.ed9d955-SNAPSHOT clean connectorJar
./gradlew -PpravegaVersion=0.6.0-50.ed9d955-SNAPSHOT jar
```

## Build Docker Image

```bash
./gradlew clean dockerBuildImage
```

# Deploy

## Prerequisites

1. Pravega cluster (not standalone) is deployed. Note the Pravega version for the compatibility.

## Steps

1.
```bash
docker run --network host -d akurilov/pravega-flink-connector-benchmark:1.0.0
```

# Jobs

Common configuration parameters (w/ default values):
* `--scope scope0`
* `--stream stream0`
* `--controllerUri tcp://127.0.0.1:9090`

## Write
    
Job FQCN: `io.pravega.flink.benchmark.PravegaWriteBenchmarkJob`

This job us running by default. Generates the events payload of the specified size with the specified rate and 
performs writes to Pravega via the Flink connector.

Specific parameters (w/ default values):
* `--evtPayloadSize 1`
* `--rateLimit 1`

## Read 

Job FQCN: `io.pravega.flink.benchmark.PravegaReadBenchmarkJob`

Accounts the specified event stream consuming rate (evt/s), reports the rate every 1 sec.

Specific parameters (w/ default values):
* `--readTimeoutMillis 100`

# Open Issues

1. How to scale? Change Flink parallelism/count of Pravega streams/etc?
2. How to measure? 