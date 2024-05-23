VERSION=v1.1.0
BRANCH=v1.1.0
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

build_docker:
	# build docker image
	./docker_build.sh -t v1.1.0 -p false