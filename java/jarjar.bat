@echo off
./gradlew bootJar
@REM java -jar your-app.jar --spring.profiles.active=iam
npx @redocly/cli build-docs openapi.yaml