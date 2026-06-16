#!/bin/bash

set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
HOST_PWD=$(pwd)
#была команда для винды сделала для убунту для ci агента

BASE_OUTPUT_DIR="./test-output/$TIMESTAMP"


cleanup() {
  echo ">>> Stopping Docker Compose environment"
  docker compose down
}

# trap cleanup EXIT

echo ">>> Preparing output folders"
mkdir -p "$LOGS_DIR" "$RESULTS_DIR" "$REPORT_DIR" "$ALLURE_RESULTS_DIR" "$ALLURE_REPORT_DIR"

#echo ">>> Pulling browser images"
#docker pull selenoid/firefox:latest
#docker pull selenoid/chrome:latest

echo ">>> Starting Docker Compose environment"
docker compose up -d backend
#frontend selenoid selenoid-ui

echo ">>> Waiting for environment to become ready"
sleep 20

#echo ">>> Running UI tests"
#TEST_PROFILE=ui docker compose run --rm \
#  -v "${HOST_PWD}/test-output/$TIMESTAMP/logs:/app/logs" \
#  -v "${HOST_PWD}/test-output/$TIMESTAMP/results:/app/target/surefire-reports" \
#  -v "${HOST_PWD}/test-output/$TIMESTAMP/report:/app/target/site" \
#  tests


TARGET_DIR="${GITHUB_WORKSPACE}/target"

mkdir -p \
  "$TARGET_DIR/surefire-reports" \
  "$TARGET_DIR/surefire-report" \
  "$TARGET_DIR/allure-results" \
  "$TARGET_DIR/allure-report" \
  "$TARGET_DIR/swagger-coverage-output" \
  "$LOGS_DIR"

echo "GITHUB_WORKSPACE: $GITHUB_WORKSPACE"




echo ">>> Running API tests"
TEST_PROFILE=api docker compose run --rm \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/logs:/app/logs" \
  -v "${TARGET_DIR}/surefire-reports:/app/target/surefire-reports" \
  -v "${TARGET_DIR}/surefire-report:/app/target/site" \
  -v "${TARGET_DIR}/allure-results:/app/target/allure-results" \
  -v "${TARGET_DIR}/allure-report:/app/target/site/allure-maven-plugin" \
  -v "${TARGET_DIR}/swagger-coverage-output:/app/target/swagger-coverage-output" \
  tests


echo ">>> All tests finished"
echo "Logs: $LOGS_DIR"
echo "Results: $RESULTS_DIR"
echo "Report: $REPORT_DIR"
echo "Allure results: $ALLURE_RESULTS_DIR"
echo "Allure report: $ALLURE_REPORT_DIR"