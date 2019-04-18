#!/usr/bin/env bash


cd source_code || echo "missing input source_code: source_code"

#echo "Using MAVEN_OPTS: ${MAVEN_OPTS}"
source /docker-lib.sh
start_docker

docker --version

whoami

docker ps

#mvn verify ${MAVEN_ARGS}

