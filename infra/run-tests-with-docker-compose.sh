#!/bin/bash

set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
HOST_PWD=$(pwd)
#была команда для винды сделала для убунту для ci агента

BASE_OUTPUT_DIR="./test-output/$TIMESTAMP"

LOGS_DIR="$BASE_OUTPUT_DIR/logs"
RESULTS_DIR="$BASE_OUTPUT_DIR/results"
REPORT_DIR="$BASE_OUTPUT_DIR/report"
ALLURE_RESULTS_DIR="$BASE_OUTPUT_DIR/allure-results"
ALLURE_REPORT_DIR="$BASE_OUTPUT_DIR/allure-report"

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


SWAGGER_COVERAGE_DIR="${GITHUB_WORKSPACE}/target/swagger-coverage-output"

mkdir -p "$LOGS_DIR" "$RESULTS_DIR" "$REPORT_DIR" "$ALLURE_RESULTS_DIR" "$ALLURE_REPORT_DIR" "$SWAGGER_COVERAGE_DIR"


echo "GITHUB_WORKSPACE: $GITHUB_WORKSPACE"
echo "SWAGGER_COVERAGE_DIR: $SWAGGER_COVERAGE_DIR"



echo ">>> Running API tests"
TEST_PROFILE=api docker compose run --rm \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/logs:/app/logs" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/results:/app/target/surefire-reports" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/report:/app/target/site" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/allure-results:/app/target/allure-results" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/allure-report:/app/target/site/allure-maven-plugin" \
  -v "${SWAGGER_COVERAGE_DIR}:/app/target/swagger-coverage-output" \
  tests


echo ">>> All tests finished"
echo "Logs: $LOGS_DIR"
echo "Results: $RESULTS_DIR"
echo "Report: $REPORT_DIR"
echo "Allure results: $ALLURE_RESULTS_DIR"
echo "Allure report: $ALLURE_REPORT_DIR"