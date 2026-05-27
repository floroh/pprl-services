# PPRL services

## Prerequisites
- Docker
- OR
  - Java 21
  - Maven

## Docker
Run services locally
(data owner: [localhost:8081](http://localhost:8081),
linkage unit [localhost:8082](http://localhost:8082),
protocol manager [localhost:8085](http://localhost:8085),
data generator unit [localhost:8086](http://localhost:8086)
)
with MongoDB in docker:
```bash
cp default.env .env
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1
docker network create pprl-services-net
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

## Data generation requirements
For usage of the data generation based on the North Carolina Voter Registry (NCVR),
the respective data must be provided either:

### Importing it from the MongoDB database provided by [Panse et al.](https://doi.org/10.5441/002/edbt.2021.67)
- see also the [respective README](db/README.NCVR.md)
- adjust the datasets.usvr.connection-string in the data-generator application.yml
- Call the endpoint of the data generator service to parse and import the record clusters
```bash
curl --request POST \
  --url http://localhost:8086/selector/prepare/import-ncvr \
  --header 'content-type: application/json' \
  --data '{}'
```
### Direct import of the parsed record clusters
- put the `ncvr_cluster.gz` in the directory `db/dumps`
```bash
docker exec -it pprl-services-mongo /data/dumps/mongorestore.sh
```

## Dev
The vanilla repository does not contain application.yml that are 
needed for running the services, because they are frequently changed in development and are therefore 
included in the gitignore. However, the repositories contain application.yml.default files, that should 
work out-of-the-box and have to be copied/renamed and can be changed if needed.
```bash
cp data-owner/src/main/resources/application.yml.default data-owner/src/main/resources/application.yml
cp linkage-unit/src/main/resources/application.yml.default linkage-unit/src/main/resources/application.yml
cp protocol-manager/src/main/resources/application.yml.default protocol-manager/src/main/resources/application.yml
cp data-generator/src/main/resources/application.yml.default data-generator/src/main/resources/application.yml

cp data-owner/src/main/resources/application-mongo.yml.default data-owner/src/main/resources/application-mongo.yml
cp linkage-unit/src/main/resources/application-mongo.yml.default linkage-unit/src/main/resources/application-mongo.yml
cp protocol-manager/src/main/resources/application-mongo.yml.default protocol-manager/src/main/resources/application-mongo.yml
cp data-generator/src/main/resources/application-mongo.yml.default data-generator/src/main/resources/application-mongo.yml
```

To build and start the services run the following commands from the base directory of the project
```bash
mvn clean install -DskipTests
mvn -f data-owner spring-boot:run
mvn -f linkage-unit spring-boot:run
mvn -f protocol-manager spring-boot:run
mvn -f data-generator spring-boot:run
```

## Use
[Data Owner](data-owner/README.md)

[Linkage Unit](linkage-unit/README.md)