# NCVR Data Generator

This project uses the huge NCVR database provided by Fabian Panse to derive smaller test datasets of a certain size, overlap and error rate.

Panse, Fabian, et al. "Generating Realistic Test Datasets for Duplicate Detection at Scale Using Historical Voter Data." EDBT. 2021.
https://vsis-www.informatik.uni-hamburg.de/getDoc.php/publications/639/p152.pdf

## Installation
The database of Panse requires a MongoDB instance which can be run using the docker-compose setup in this repository.

### Prerequisites
- Install docker and docker-compose
- Make sure to have enough free disk space for the unpacked json import files ("Data Size") and the MongoDB database ("Storage Size").
  - raw import: Data Size 326 GB, Storage Size 73.2 GB

### Import

1. Unzip the nctestdata-modX.zip files to `us-data/nc`
(Adapt the paths in docker-compose.yml if the original files and the MongoDB database are located elsewhere)

2. Run MongoDB:
```
docker compose -f docker-compose.ncvr-db.yml up -d
```

3. Import the raw data into MongoDB (May take a while...)
```
docker exec -it us-mongo-db bash /importdata/nc/mongoimport-all.sh
```
