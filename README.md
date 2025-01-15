# PPRL Services (BTW 2025 edition)

This repository contains the code for the paper
_"Multi-Layer Privacy-Preserving Record Linkage with Clerical Review based on gradual information disclosure"_
by Florens Rohde, Victor Christen, Martin Franke and Erhard Rahm.
The paper is accepted at the [BTW 2025](https://btw2025.gi.de/) conference.
The datasets used for the experimental evaluation are available
[here](https://cloud.scadsai.uni-leipzig.de/index.php/s/wZyRnRkP9EaMqsM/download/datasets-btw2025-pprl.zip).

The services are developed by the [Database Group](https://dbs.uni-leipzig.de/research/projects/pprl) of the University of Leipzig, Germany.
Please note, that the documentation is still under construction.

## Prerequisites
- Docker
- OR
- Java 21
- Maven

## Docker
Run both services locally
(data owner: [localhost:8081](http://localhost:8081),
linkage unit [localhost:8082](http://localhost:8082))
with MongoDB in docker:
```bash
docker compose up
```
or after changes to the code:
```bash
docker compose up --build
```

Run only one service using docker, e.g., the data-owner:
```bash
docker compose up mongo pprl-do
```

## Dev
The vanilla repositories for the Data Owner and Linkage Unit do not contain application.yml that are 
needed for running the services, because they are frequently changed in development and are therefore 
included in the gitignore. However, the repositories contain application.yml.default files, that should 
work out-of-the-box and have to be copied/renamed and can be changed if needed.
```bash
cp data-owner/src/main/resources/application.yml.default data-owner/src/main/resources/application.yml
cp linkage-unit/src/main/resources/application.yml.default linkage-unit/src/main/resources/application.yml
```

To build and start the services run the following commands from the base directory of the project
```bash
mvn clean install -DskipTests
mvn -f data-owner spring-boot:run
mvn -f linkage-unit spring-boot:run
```

## Use
[Data Owner](data-owner/README.md)

[Linkage Unit](linkage-unit/README.md)