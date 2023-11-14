@echo off
./gradlew bootJar
docker-compose -f ELK.yml up -d
@REM java -jar your-app.jar --spring.profiles.active=iam
@REM npx @redocly/cli build-docs openapi.yaml