#!/bin/bash
MONGO_VERSION=$(mongosh --quiet --eval "db.version()")
SAFE_VERSION=$(echo "$MONGO_VERSION" | tr -d '\n' | tr '.' '_')
OUTPUT_FILE="/data/dumps/ncvr_clusters__${SAFE_VERSION}.gz"

mongodump \
  --authenticationDatabase=admin \
  --db=pprldg \
  --collection=ncvr_clusters \
  --archive="$OUTPUT_FILE" \
  --gzip \
  "mongodb://root:example@localhost:27017"