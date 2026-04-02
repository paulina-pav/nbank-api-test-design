#!/bin/bash

set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
HOST_PWD=$(pwd)

BASE_OUTPUT_DIR="./test-output/$TIMESTAMP"

LOGS_DIR="$BASE_OUTPUT_DIR/logs"
RESULTS_DIR="$BASE_OUTPUT_DIR/results"
REPORT_DIR="$BASE_OUTPUT_DIR/report"
SWAGGER_DIR="$BASE_OUTPUT_DIR/swagger-coverage-output"

cleanup() {
  echo ">>> Stopping Docker Compose environment"
  docker compose down
}

trap cleanup EXIT

echo ">>> Preparing output folders"
mkdir -p "$LOGS_DIR" "$RESULTS_DIR" "$REPORT_DIR" "$SWAGGER_DIR"

echo ">>> Pulling browser images"
docker pull selenoid/firefox:latest
docker pull selenoid/chrome:latest

echo ">>> Starting Docker Compose environment"
docker compose up -d backend frontend nginx selenoid selenoid-ui

echo ">>> Waiting for environment to become ready"
sleep 60

echo ">>> Running UI tests"
TEST_PROFILE=ui docker compose run --rm \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/logs:/app/logs" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/results:/app/target/surefire-reports" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/report:/app/target/site" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/swagger-coverage-output:/app/target/swagger-coverage-output" \
  tests

echo ">>> Running API tests"
TEST_PROFILE=api docker compose run --rm \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/logs:/app/logs" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/results:/app/target/surefire-reports" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/report:/app/target/site" \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/swagger-coverage-output:/app/target/swagger-coverage-output" \
  tests

echo ">>> All tests finished"
echo "$BASE_OUTPUT_DIR" > .last-test-output-dir
echo "Logs: $LOGS_DIR"
echo "Results: $RESULTS_DIR"
echo "Report: $REPORT_DIR"
echo "Swagger coverage output: $SWAGGER_DIR"