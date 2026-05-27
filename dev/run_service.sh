#!/bin/sh
# run-service.sh
# Usage: ./run-service.sh <service-name> <target_dir>

SERVICE=$1
TARGET_DIR=${2:-/opt/pprl-services}  # default to /opt/pprl-services if not specified
cd "$TARGET_DIR" || exit 1

case "$SERVICE" in
  do) java -jar data-owner.jar --spring.config.location=file:data-owner.yml ;;
  lu) java -jar linkage-unit.jar --spring.config.location=file:linkage-unit.yml ;;
  pm) java -jar protocol-manager.jar --spring.config.location=file:protocol-manager.yml ;;
  dg) java -jar data-generator.jar --spring.config.location=file:data-generator.yml ;;
  *) echo "Unknown service"; exit 1 ;;
esac