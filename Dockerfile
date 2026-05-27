# build environment
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /src
COPY pom.xml pom.xml
COPY pprl-core/pom.xml pprl-core/
COPY common/pom.xml common/
COPY data-owner/pom.xml data-owner/
COPY linkage-unit/pom.xml linkage-unit/
COPY data-generator/pom.xml data-generator/
COPY protocol-manager/pom.xml protocol-manager/
COPY pprl-core/pom.xml pprl-core/
COPY pprl-core/pprl-core-common/pom.xml pprl-core/pprl-core-common/
COPY pprl-core/pprl-core-analyzer/pom.xml pprl-core/pprl-core-analyzer/
COPY pprl-core/pprl-core-encoder/pom.xml pprl-core/pprl-core-encoder/
COPY pprl-core/pprl-core-matcher/pom.xml pprl-core/pprl-core-matcher/

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f pom.xml dependency:go-offline -DskipTests

# Copy rest of sources
COPY pprl-core pprl-core
COPY common common
COPY data-owner data-owner
COPY linkage-unit linkage-unit
COPY data-generator data-generator
COPY protocol-manager protocol-manager

# Build
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS pprl-runner
RUN apk add --no-cache tzdata
ENV TZ=Europe/Berlin
WORKDIR /opt/pprl-services

# ── Data Owner ────────────────────────────────────────────────────────
FROM pprl-runner AS pprl-do-extractor
WORKDIR /extract
COPY --from=build /src/data-owner/target/*-spring-boot.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM pprl-runner AS pprl-do-runner
COPY --from=pprl-do-extractor /extract/dependencies/ ./
COPY --from=pprl-do-extractor /extract/spring-boot-loader/ ./
COPY --from=pprl-do-extractor /extract/snapshot-dependencies/ ./
COPY --from=pprl-do-extractor /extract/application/ ./
COPY data-owner/application-docker.yml application.yml
COPY data-owner/src/main/resources/configs do/configs
COPY data-owner/src/main/resources/logback.xml .
COPY data-owner/src/main/resources/data do/data
COPY data-owner/src/main/resources/pprl-data-owner.jks do/pprl-data-owner.jks
EXPOSE 8081
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# ── Linkage Unit ──────────────────────────────────────────────────────
FROM pprl-runner AS pprl-lu-extractor
WORKDIR /extract
COPY --from=build /src/linkage-unit/target/*-spring-boot.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM pprl-runner AS pprl-lu-runner
COPY --from=pprl-lu-extractor /extract/dependencies/ ./
COPY --from=pprl-lu-extractor /extract/spring-boot-loader/ ./
COPY --from=pprl-lu-extractor /extract/snapshot-dependencies/ ./
COPY --from=pprl-lu-extractor /extract/application/ ./
COPY linkage-unit/application-docker.yml application.yml
COPY linkage-unit/src/main/resources/configs lu/configs
COPY linkage-unit/src/main/resources/logback.xml .
EXPOSE 8082
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# ── Protocol Manager ──────────────────────────────────────────────────
FROM pprl-runner AS pprl-pm-extractor
WORKDIR /extract
COPY --from=build /src/protocol-manager/target/*-spring-boot.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM pprl-runner AS pprl-pm-runner
COPY --from=pprl-pm-extractor /extract/dependencies/ ./
COPY --from=pprl-pm-extractor /extract/spring-boot-loader/ ./
COPY --from=pprl-pm-extractor /extract/snapshot-dependencies/ ./
COPY --from=pprl-pm-extractor /extract/application/ ./
COPY protocol-manager/application-docker.yml application.yml
COPY protocol-manager/src/main/resources/logback.xml .
EXPOSE 8085
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# ── Data Generator ────────────────────────────────────────────────────
FROM pprl-runner AS pprl-dg-extractor
WORKDIR /extract
COPY --from=build /src/data-generator/target/*-spring-boot.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM pprl-runner AS pprl-dg-runner
COPY --from=pprl-dg-extractor /extract/dependencies/ ./
COPY --from=pprl-dg-extractor /extract/spring-boot-loader/ ./
COPY --from=pprl-dg-extractor /extract/snapshot-dependencies/ ./
COPY --from=pprl-dg-extractor /extract/application/ ./
COPY data-generator/application-docker.yml application.yml
COPY data-generator/src/main/resources/logback.xml .
EXPOSE 8086
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# production environment for all services
FROM pprl-runner AS pprl-service-runner

COPY --from=build /src/data-owner/target/*-spring-boot.jar data-owner.jar
COPY data-owner/application-docker.yml data-owner.yml
COPY data-owner/src/main/resources/configs do/configs
COPY data-owner/src/main/resources/logback.xml data-owner-logback.xml
COPY data-owner/src/main/resources/data do/data
COPY data-owner/src/main/resources/pprl-data-owner.jks do/pprl-data-owner.jks

COPY --from=build /src/linkage-unit/target/*-spring-boot.jar linkage-unit.jar
COPY linkage-unit/application-docker.yml linkage-unit.yml
COPY linkage-unit/src/main/resources/configs lu/configs
COPY linkage-unit/src/main/resources/logback.xml linkage-unit-logback.xml

COPY --from=build /src/protocol-manager/target/*-spring-boot.jar protocol-manager.jar
COPY protocol-manager/application-docker.yml protocol-manager.yml
COPY protocol-manager/src/main/resources/logback.xml protocol-manager-logback.xml

COPY --from=build /src/data-generator/target/*-spring-boot.jar data-generator.jar
COPY data-generator/application-docker.yml data-generator.yml
COPY data-generator/src/main/resources/logback.xml data-generator-logback.xml

COPY dev/run_service.sh run_service.sh
RUN chmod +x run_service.sh
ENTRYPOINT ["/opt/pprl-services/run_service.sh"]
#CMD ["sleep", "infinity"]