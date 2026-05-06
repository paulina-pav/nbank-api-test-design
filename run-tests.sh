#!/bin/bash

mkdir -p /app/logs
if [ "$TEST_PROFILE" = "ui" ]; then
  PARALLEL=2
else
  PARALLEL=5
fi

{
  echo ">>> Running tests with profile: ${TEST_PROFILE}"
  echo ">>> APIBASEURL: ${APIBASEURL}"
  echo ">>> UIBASEURL: ${UIBASEURL}"
  echo ">>> Current directory: $(pwd)"
  echo ">>> Files in /app: $(ls -la /app/)"

  echo ">>> Commit in container:"
  git rev-parse HEAD || echo "no git repo"

  mvn test -q -P "${TEST_PROFILE}" \
    -Djunit.jupiter.execution.parallel.enabled=true \
    -Djunit.jupiter.execution.parallel.mode.default=concurrent \
    -Djunit.jupiter.execution.parallel.config.strategy=fixed \
    -Djunit.jupiter.execution.parallel.config.fixed.parallelism=${PARALLEL}
    -Djunit.jupiter.execution.parallel.mode.classes.default=concurrent \
    -Djunit.jupiter.execution.parallel.mode.default=same_thread \

  echo ">>> Running surefire-report:report"
  mvn -DskipTests=true surefire-report:report

} > /app/logs/run.log 2>&1

cat /app/logs/run.log