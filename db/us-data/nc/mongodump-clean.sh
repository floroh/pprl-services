#!/bin/bash
#mongodump --authenticationDatabase=admin --db=usvr --collection=nc-clean  --out=/importdata/dump mongodb://root:example@localhost:27017

# Compressed
mongodump --authenticationDatabase=admin --db=usvr --collection=nc-clean --archive=/importdata/dump/nc-clean.gz --gzip mongodb://root:example@localhost:27017