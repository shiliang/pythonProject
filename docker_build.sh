#!/bin/bash

# 检查参数数量是否正确
if [ $# -lt 1 ]; then
    echo "Usage: $0 <please input tag>"
    exit 1
fi

docker_tag=$1

is_push=$3

# 设置默认的 Docker register 地址
docker_register_address=$3

git pull && mvn clean package

docker build -f ./Dockerfile-simple -t mira/mira-job-service:${docker_tag} .


if [ ! -z "$docker_register_address" ]; then
  #tag mira-backend-service-build
    docker tag mira/mira-job-service:${docker_tag} ${docker_register_address}/mira/mira-job-service:${docker_tag}
fi

#是否push
if [ "$is_push" == "true" ]; then
  # 假设$docker_register_address 为空，则默认为docker hub
  if [ -z "$docker_register_address" ]; then
    docker push mira/mira-job-service:${docker_tag}
  else
    docker push ${docker_register_address}/mira/mira-job-service:${docker_tag}
  fi
fi
