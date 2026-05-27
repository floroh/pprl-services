#!/bin/bash
mongorestore --authenticationDatabase=admin --gzip --archive=/data/dumps/ncvr_clusters.gz mongodb://root:example@localhost:27017
