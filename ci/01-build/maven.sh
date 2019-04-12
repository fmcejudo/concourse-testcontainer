#!/usr/bin/env bash


cd source_code || echo "missing input source_code: source_code"

#echo "Using MAVEN_OPTS: ${MAVEN_OPTS}"

docker --version

whoami

docker ps

#mvn verify ${MAVEN_ARGS}

