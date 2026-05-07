#!/bin/bash

mkdir -p /app/logs

if [ "$TEST_PROFILE" = "api" ]; then
  PARALLEL_ARGS="\
    -Djunit.jupiter.execution.parallel.enabled=false \
    -Djunit.jupiter.execution.parallel.mode.default=concurrent \
    -Djunit.jupiter.execution.parallel.config.strategy=fixed \
    -Djunit.jupiter.execution.parallel.config.fixed.parallelism=4"
else
  PARALLEL_ARGS="\
    -Djunit.jupiter.execution.parallel.enabled=false"
fi

{
  echo ">>> Running tests with profile: ${TEST_PROFILE}"
  echo ">>> APIBASEURL: ${APIBASEURL}"
  echo ">>> UIBASEURL: ${UIBASEURL}"
  echo ">>> Parallel args: ${PARALLEL_ARGS}"
  echo ">>> Current directory: $(pwd)"
  echo ">>> Files in /app: $(ls -la /app/)"

  echo ">>> Commit in container:"
  git rev-parse HEAD || echo "no git repo"

  mvn test -q -P "${TEST_PROFILE}" ${PARALLEL_ARGS}

  echo ">>> Running surefire-report:report"
  mvn -DskipTests=true surefire-report:report

} > /app/logs/run.log 2>&1

cat /app/logs/run.log