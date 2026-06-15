#!/bin/sh

mkdir -p /app/logs

{
  echo ">>> Running tests with profile: ${TEST_PROFILE}"
  echo ">>> APIBASEURL: ${APIBASEURL}"
  echo ">>> UIBASEURL: ${UIBASEURL}"
  echo ">>> Current dir before tests:"
  pwd

  mvn test -q -P "${TEST_PROFILE}"

  echo ">>> Searching swagger coverage files inside container"
  pwd
  find /app -name "*coverage.json" -o -name "swagger-coverage-output" || true
  echo ">>> Listing /app/target"
  ls -la /app/target || true
  echo ">>> Listing /app/target/swagger-coverage-output"
  ls -la /app/target/swagger-coverage-output || true

  echo ">>> Running surefire-report:report"
  mvn -DskipTests=true surefire-report:report

  echo ">>> Running allure:report"
  mvn -DskipTests=true allure:report
} > /app/logs/run.log 2>&1