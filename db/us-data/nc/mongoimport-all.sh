#!/bin/bash
ls -1 /importdata/nc/*.json | grep 'nctestdata*' | while read jsonfile; do
    echo Importing: $jsonfile;
    mongoimport --authenticationDatabase=admin --type=json --db=usvr --collection=2-testdata mongodb://root:example@localhost:27017 $jsonfile
done