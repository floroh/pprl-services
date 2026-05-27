#!/bin/bash
source .env

# Pull build image
docker pull maven:3.9-eclipse-temurin-21-alpine

# Pull runner image
docker pull eclipse-temurin:21-jre-alpine

# Pull MongoDB image
docker pull mongo:8.0