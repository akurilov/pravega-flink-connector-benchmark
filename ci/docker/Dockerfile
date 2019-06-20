ARG FLINK_VERSION

FROM openjdk:8-jdk-alpine
ADD src ./src/
ADD gradle ./gradle/
ADD build.gradle ./build.gradle
ADD gradlew ./gradlew
ADD settings.gradle ./settings.gradle
RUN ./gradlew clean connectorJar && ./gradlew jar

FROM flink:${FLINK_VERSION}
COPY --from=0 /build/libs/pravega-flink-connector-benchmark.jar /opt/pravega-flink-connector-benchmark/pravega-flink-connector-benchmark.jar
ADD ci/docker/entrypoint.sh /opt/mongoose/entrypoint.sh
