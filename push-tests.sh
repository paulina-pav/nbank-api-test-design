#!/bin/bash

set -e

IMAGE_NAME="nbank-tests"
DOCKERHUB_USERNAME="tilemp"
TAG="latest"

REMOTE_IMAGE="${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${TAG}"

if [ -z "$DOCKERHUB_TOKEN" ]; then
  echo "ERROR: DOCKERHUB_TOKEN is not set"
  echo "Run first:"
  echo "export DOCKERHUB_TOKEN=your_token"
  exit 1
fi

echo ">>> Logging in to Docker Hub"
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

echo ">>> Tagging image"
docker tag "${IMAGE_NAME}:${TAG}" "$REMOTE_IMAGE"

echo ">>> Pushing image"
docker push "$REMOTE_IMAGE"

echo ">>> Push completed successfully"
echo "Pull command:"
echo "docker pull $REMOTE_IMAGE"