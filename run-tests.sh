#!/bin/sh

mkdir -p /app/logs

{
  echo ">>> Running tests with profile: ${TEST_PROFILE}"
  echo ">>> APIBASEURL: ${APIBASEURL}"
  echo ">>> UIBASEURL: ${UIBASEURL}"

  mvn test -q -P "${TEST_PROFILE}"

  echo ">>> Running surefire-report:report"
  mvn -DskipTests=true surefire-report:report
} > /app/logs/run.log 2>&1