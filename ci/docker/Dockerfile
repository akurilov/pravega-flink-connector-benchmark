ARG FLINK_VERSION

FROM openjdk:8-jdk-alpine
ADD src ./src/
ADD gradle ./gradle/
ADD build.gradle ./build.gradle
ADD gradlew ./gradlew
ADD gradle.properties ./gradle.properties
ADD settings.gradle ./settings.gradle
RUN ./gradlew clean connectorJar && ./gradlew jar
RUN ls -l build/libs

FROM flink:${FLINK_VERSION}
ARG VERSION
COPY --from=0 /build/libs/pravega-flink-connector-benchmark-${VERSION}.jar /opt/pravega-flink-connector-benchmark/pravega-flink-connector-benchmark.jar
ADD ci/docker/entrypoint.sh /entrypoint.sh
ENTRYPOINT /entrypoint.sh
