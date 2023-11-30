# This is the equivalent of '@echo off' in batch
$ErrorActionPreference = "SilentlyContinue"

$env:LOGSTASH_HOST="localhost"
./gradlew clean test jacocoTestReport
