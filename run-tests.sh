#!/bin/bash

mkdir -p /app/logs

{
  echo ">>> Running tests with profile: ${TEST_PROFILE}"
  echo ">>> Parallel execution: disabled"
  echo ">>> APIBASEURL: ${APIBASEURL}"
  echo ">>> UIBASEURL: ${UIBASEURL}"
  echo ">>> Current directory: $(pwd)"
  echo ">>> Files in /app: $(ls -la /app/)"

  echo ">>> Commit in container:"
  git rev-parse HEAD || echo "no git repo"

  mvn test -q -P "${TEST_PROFILE}"

  echo ">>> Running surefire-report:report"
  mvn -DskipTests=true surefire-report:report

} > /app/logs/run.log 2>&1

cat /app/logs/run.log