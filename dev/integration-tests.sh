#!/bin/sh
set -e
BASE_DO="http://pprl-do:8081"
BASE_LU="http://pprl-lu:8082"
BASE_PM="http://pprl-pm:8085"
BASE_DG="http://pprl-dg:8086"
NETWORK="pprl-services-net"

dcurl() {
  docker run --rm --network "$NETWORK" curlimages/curl "$@"
}

FAILED=0

check() {
  label=$1
  shift
  if eval "$@"; then
    printf "\n%s: OK\n" "$label"
  else
    printf "\n%s: FAILED\n" "$label"
    FAILED=1
  fi
}

echo "=== Health checks ==="
check "DO healthy" dcurl -sf $BASE_DO/actuator/health
check "LU healthy" dcurl -sf $BASE_LU/actuator/health
check "PM healthy" dcurl -sf $BASE_PM/actuator/health
check "DG healthy" dcurl -sf $BASE_DG/actuator/health

echo "=== API calls ==="
check "DO configs found" "dcurl -sf --request GET --url $BASE_DO/config/findAll | grep -q 'DBSLeipzig'"

if [ $FAILED -ne 0 ]; then
  echo "=== Some checks FAILED ==="
  exit 1
fi

echo "=== All checks passed ==="