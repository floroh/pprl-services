# build environment
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /src
COPY pom.xml pom.xml
COPY pprl-core pprl-core
COPY common common
COPY data-owner data-owner
COPY linkage-unit linkage-unit
COPY protocol-manager protocol-manager
RUN mvn clean package -DskipTests

# production environment for data owner
FROM eclipse-temurin:21-jre-alpine AS pprl-do-runner
WORKDIR /pprl-do-service
RUN apk add --no-cache tzdata
ENV TZ=Europe/Berlin
COPY --from=build /src/data-owner/target/*-spring-boot.jar app.jar
COPY data-owner/application-docker.yml application.yml
COPY data-owner/src/main/resources/configs configs
COPY data-owner/src/main/resources/logback.xml .
COPY data-owner/src/main/resources/data data
COPY data-owner/src/main/resources/pprl-data-owner.jks .
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]

# production environment for linkage unit
FROM eclipse-temurin:21-jre-alpine AS pprl-lu-runner
WORKDIR /pprl-lu-service
RUN apk add --no-cache tzdata
ENV TZ=Europe/Berlin
COPY --from=build /src/linkage-unit/target/*-spring-boot.jar app.jar
COPY linkage-unit/application-docker.yml application.yml
COPY linkage-unit/src/main/resources/logback.xml .
COPY linkage-unit/src/main/resources/configs configs
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]

# production environment for protocol manager
FROM eclipse-temurin:21-jre-alpine AS pprl-pm-runner
WORKDIR /pprl-pm-service
RUN apk add --no-cache tzdata
ENV TZ=Europe/Berlin
COPY --from=build /src/protocol-manager/target/*-spring-boot.jar app.jar
COPY protocol-manager/application-docker.yml application.yml
COPY protocol-manager/src/main/resources/logback.xml .
EXPOSE 8085
ENTRYPOINT ["java","-jar","app.jar"]