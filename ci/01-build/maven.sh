#!/usr/bin/env sh

cd source_code || echo "missing input source_code: source_code"

#echo "Using MAVEN_OPTS: ${MAVEN_OPTS}"

whoami

. docker-entrypoint.sh

start_docker


docker ps

#mvn verify ${MAVEN_ARGS}

