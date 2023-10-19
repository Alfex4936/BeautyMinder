@echo off
./gradlew bootJar

npx @redocly/cli build-docs openapi.yaml 