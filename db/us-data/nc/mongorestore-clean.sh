#!/bin/bash
mongorestore --authenticationDatabase=admin --nsInclude=usvr.nc-clean mongodb://root:example@localhost:27017 /importdata/dump/

# Compressed
#mongorestore --authenticationDatabase=admin --gzip --archive=/importdata/dump/2-testdata-clean.gz --nsInclude=ncvr.2-testdata-clean mongodb://root:example@localhost:27017
