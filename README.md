# pravega-flink-connector-benchmark
Pravega Flink Connector Benchmark

# Build

```bash
./gradlew jar
```

## Build With Specific Pravega Version

```bash
./gradlew -PpravegaVersion=0.6.0-50.ed9d955-SNAPSHOT clean jar
```

# Deploy

## Prerequisites

1. Pravega cluster (not standalone) is deployed. Note the Pravega version for the compatibility.
2. Flink cluster/standalone is deployed.

## Steps

1. Open Flink UI in the browser. Use the Flink's *master node* address and the port # 8081.
2. Go to the *Submit new Job* tab in the left menu.
3. Press the *Add New +* button.
4. Select the built jar in the open file dialog.
5. Upload the jar.
6. Specify the entry class for the selected job, the desired parallelism and arguments.
7. Press the *Submit* button.

![](Screenshot_20190604_162856.png)

# Jobs

* `io.pravega.flink.benchmark.MessageRateAccountingJob`

    Accounts the specified event stream consuming rate (evt/s), reports the rate every 1 sec.

    Parameters (w/ default values):
    * `--scope="scope0"`
    * `--stream="stream0"`
    * `--controllerUri="tcp://127.0.0.1:9090"`
    * `--readTimeoutMillis`
