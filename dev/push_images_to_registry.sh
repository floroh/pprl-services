#!/usr/bin/env bash
set -euo pipefail

# ---------- CONFIG ----------
# Registry / organization prefix (change to your registry)
REGISTRY="${DOCKER_REGISTRY:-docker.scadsai.uni-leipzig.de}"
ORG="${DOCKER_ORG:-pprl-dev}"
# The canonical tag expected to have been used for the build (can come from CI env)
ENV_TAG="${DOCKER_IMAGE_TAG:-}"
# If true, will just echo commands (for debugging). Set to "true" to dry-run.
DRY_RUN="${DRY_RUN:-false}"
# Timezone for timestamp (Berlin as requested)
TZ="Europe/Berlin"
# ----------------------------

usage() {
  cat <<EOF
Usage: $0 <image-name> [<image-name> ...]
Example: $0 pprl-services pprl-do-service
Notes:
 - The script expects docker compose to build images defined in your docker-compose.yml
 - The image names passed should be the repository name portion)
 - The canonical tag should be in \DOCKER_IMAGE_TAG (env) or provided below
EOF
  exit 1
}

if [ $# -lt 1 ]; then
  usage
fi

# Compute canonical tag (must exist). If not set, fail early.
if [ -z "${ENV_TAG}" ]; then
  echo "ERROR: DOCKER_IMAGE_TAG is not set. Set it in env (e.g. 1.2.3) or in the script."
  exit 2
fi

# Compute a single timestamp once (format vYYYY.MM.DD-HHMM)
TIMESTAMP="v$(TZ=${TZ} date +%Y.%m.%d-%H%M)"

echo "Using canonical tag: ${ENV_TAG}"
echo "Using timestamp tag: ${TIMESTAMP}"
echo "Registry: ${REGISTRY}/${ORG}"
echo

# Helper: run or echo command depending on DRY_RUN
run() {
  if [ "${DRY_RUN}" = "true" ]; then
    echo "+ $*"
  else
    "$@"
  fi
}

# Build everything with docker compose (will use image: names in compose file)
echo "Building images via docker compose..."
run docker compose build --pull
run docker build --pull --target pprl-service-runner -t ${DOCKER_REGISTRY}/${DOCKER_ORG}/pprl-services:${DOCKER_IMAGE_TAG:-latest} .

# Function to tag & push an image with timestamp. Arguments:
#   $1 = repository name (e.g. infrastructure-proxy-app)
push_image_with_timestamp() {
  local repo_name="$1"
  local full_repo="${REGISTRY}/${ORG}/${repo_name}"
  local canonical_tag="${ENV_TAG}"
  local timestamp_tag="${TIMESTAMP}"

  local canonical_ref="${full_repo}:${canonical_tag}"
  local timestamp_ref="${full_repo}:${timestamp_tag}"

  echo "=== Processing ${full_repo} ==="

  # Check that the image with the canonical tag exists locally (built by compose)
  if ! docker image inspect "${canonical_ref}" > /dev/null 2>&1; then
    echo "ERROR: expected image ${canonical_ref} not found locally. Make sure docker compose used the same image name and tag."
    echo "You can run: docker images | grep '${full_repo}'"
    exit 3
  fi

  # Create timestamp tag pointing at same image ID
  echo "Tagging ${canonical_ref} -> ${timestamp_ref}"
  run docker tag "${canonical_ref}" "${timestamp_ref}"

  # Push both tags (order: canonical then timestamp)
  echo "Pushing ${canonical_ref}"
  run docker push "${canonical_ref}"

  echo "Pushing ${timestamp_ref}"
  run docker push "${timestamp_ref}"

  echo "Done: ${canonical_ref} and ${timestamp_ref}"
  echo
}

# Iterate over supplied image names
for img in "$@"; do
  push_image_with_timestamp "${img}"
done

echo "All done. Images were tagged with timestamp ${TIMESTAMP}."

