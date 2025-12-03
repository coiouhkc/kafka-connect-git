FROM maven:3.9.11-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml pom.xml
COPY src ./src
RUN mvn clean package

FROM apache/kafka:4.0.0
WORKDIR /opt/kafka
RUN mkdir -p /opt/kafka/plugins/kafka-connector-git
COPY --from=build /app/target/kafka-connect-git-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/kafka/plugins/kafka-connector-git/
RUN mkdir -p /tmp/config
COPY config/connect-distributed.properties /tmp/config/