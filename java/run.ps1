# This is the equivalent of '@echo off' in batch
$ErrorActionPreference = "SilentlyContinue"

# This runs './gradlew bootJar' command
./gradlew clean bootJar build -x test

copy Dockerfile.spring build/libs/
copy Dockerfile.python python/

# This runs 'docker-compose -f ELK.yml up -d' command
docker-compose -f ELK.yml up --build -d
