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
  - with cleaned collection: Data Size 363 GB, Storage Size 81.7 GB
  - cleaned collection only: Data Size 37 GB, Storage Size 8.5 GB

### Import

1. Unzip the nctestdata-modX.zip files to `nctestdata/`
(Adapt the paths in docker-compose.yml if the original files and the MongoDB database are located elsewhere)

2. Run MongoDB:
```
docker compose -f docker-compose.ncvr-db.yml up -d
```

3. Import the raw data into MongoDB (May take a while...)
```
docker exec -it us-mongo-db bash /importdata/nc/mongoimport-all.sh
```

4. Run the cleaning by executing [NcvrCleaner.main()](src/main/java/de/unileipzig/dbs/pprl/service/generator/selection/services/nc/NcvrCleaner.java)

### Optional: Move cleaned collection
The huge imported collection `2-testdata` is no longer needed so it can be deleted.
If the MongoDB storage location for the import process was an external drive,
it might be a good idea to move the much smaller cleaned collection `2-testdata-clean`
to an internal SSD for faster access.

1. Dump collection
```
docker exec -it us-mongo-db bash /importdata/nc/mongodump-clean.sh
```

2. Stop MongoDB
```
docker-compose down
```

3. Prepare new docker bind mounts
- Change mount paths in docker-compose 
- Copy dumped collection to `nctestdata/dump`

4. Start MongoDB
```
docker-compose up -d
```

5. Restore collection
```
docker exec -it us-mongo-db bash /importdata/nc/mongorestore-clean.sh
```

6. Delete dump files in `nctestdata/dump`

## Usage
1. Adapt and run [NcvrGenerator.main()](src/main/java/de/unileipzig/dbs/pprl/service/generator/selection/scripts/nc/NcvrGenerator.java)

## Authors and acknowledgment
- Martin Franke (Main author)
- Florens Rohde (Installation helpers)