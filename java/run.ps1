# This is the equivalent of '@echo off' in batch
$ErrorActionPreference = "SilentlyContinue"

# This runs './gradlew bootJar' command
./gradlew clean bootJar build -x test

copy Dockerfile.spring build/libs/
copy google-beautyminder.json build/libs/
copy Dockerfile.python python/

# This runs 'docker-compose -f ELK.yml up -d' command
$userChoice = Read-Host "Do you want to run the docker? (y/n)"
if ($userChoice -eq "y") {
    docker-compose -f ELK.yml up --build -d
} else {
    docker-compose -f ELK.yml up --build --no-start
}
