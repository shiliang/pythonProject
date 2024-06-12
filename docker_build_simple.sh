#!/bin/sh

IMAGE_NAME=chainweaver/mira-job-service
IMAGE_TAG=v1.1.0
# base image tag which is related to Dockerfile_simple
BASE_IMAGE_TAG=v1.1.0_base

# check if base image exists
if [ ! "$(docker images -q ${IMAGE_NAME}:${BASE_IMAGE_TAG} 2> /dev/null)" ]; then
  if [ ! "$(docker images -q ${IMAGE_NAME}:${IMAGE_TAG} 2> /dev/null)" ]; then
     make docker_build
  fi
  docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:${BASE_IMAGE_TAG}
fi

# build binary locally
make build_local

# build docker image from Dockerfile_simple
docker build -f ./Dockerfile_simple -t ${IMAGE_NAME}:${IMAGE_TAG} .