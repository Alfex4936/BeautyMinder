# PowerShell script to fetch OpenAPI documentation and build HTML docs
$currentDate = Get-Date -Format "yyyyMMdd"
curl http://ec2-43-202-92-163.ap-northeast-2.compute.amazonaws.com:8080/v3/api-docs -o openapi.json
npx @redocly/cli@latest build-docs openapi.json -o "./API-$currentDate.html"
