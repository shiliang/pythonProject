VERSION=v1.1.1
BRANCH=v1.1.1
OS := $(shell uname -s)
PLATFORM=$(shell uname -m)
ARCH := $(shell uname -m)
DATETIME=$(shell date "+%Y/%m/%d %H:%M:%S")
GIT_BRANCH = $(shell git rev-parse --abbrev-ref HEAD)
GIT_COMMIT = $(shell git log --pretty=format:'%h' -n 1)

# 本地编译
build:
	mvn clean package && mvn package -Dmaven.test.skip=true

# 本地编译 depend on vendor
build_local:
	mvn package -Dmaven.test.skip=true

# 构建 Docker 镜像
build_docker:
	# build docker image
	./docker_build.sh -t ${VERSION} -p false

# 构建简单版 Docker 镜像
build_docker_simple: build_local
	# build simple docker image
	./docker_build.sh -t ${VERSION} -p false -d Dockerfile_simple
