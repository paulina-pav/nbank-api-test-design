#!/bin/bash
# Выше был #!/bin/sh, но для надежности используем bash

mkdir -p /app/logs

{
  echo ">>> Running tests with profile: ${TEST_PROFILE}"
  echo ">>> APIBASEURL: ${APIBASEURL}"
  echo ">>> UIBASEURL: ${UIBASEURL}"
  echo ">>> Current directory: $(pwd)"
  echo ">>> Files in /app: $(ls -la /app/)"

  mvn test -q -P "${TEST_PROFILE}"

  echo ">>> Running surefire-report:report"
  mvn -DskipTests=true surefire-report:report
} > /app/logs/run.log 2>&1

# Выводим лог на экран для отладки
cat /app/logs/run.log