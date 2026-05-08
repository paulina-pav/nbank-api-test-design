#!/bin/bash

set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
HOST_PWD=$(pwd)

BASE_OUTPUT_DIR="./test-output/api-$TIMESTAMP"

LOGS_DIR="$BASE_OUTPUT_DIR/logs"
RESULTS_DIR="$BASE_OUTPUT_DIR/results"
REPORT_DIR="$BASE_OUTPUT_DIR/report"
SWAGGER_DIR="$BASE_OUTPUT_DIR/swagger-coverage-output"
ALLURE_RESULTS_DIR="$BASE_OUTPUT_DIR/allure-results"

cleanup() {
  echo ">>> Stopping Docker Compose environment"
  docker compose down
}

trap cleanup EXIT

echo ">>> Preparing API output folders"
mkdir -p "$LOGS_DIR" "$RESULTS_DIR" "$REPORT_DIR" "$SWAGGER_DIR" "$ALLURE_RESULTS_DIR"

echo ">>> Building tests image"
docker compose build tests

echo ">>> Starting backend"
docker compose up -d backend

echo ">>> Waiting for backend"
sleep 20

echo ">>> Running API tests"
TEST_PROFILE=api docker compose run --rm \
  -v "${HOST_PWD}/test-output/api-$TIMESTAMP/logs:/app/logs" \
  -v "${HOST_PWD}/test-output/api-$TIMESTAMP/results:/app/target/surefire-reports" \
  -v "${HOST_PWD}/test-output/api-$TIMESTAMP/report:/app/target/site" \
  -v "${HOST_PWD}/test-output/api-$TIMESTAMP/swagger-coverage-output:/app/target/swagger-coverage-output" \
  -v "${HOST_PWD}/test-output/api-$TIMESTAMP/allure-results:/app/target/allure-results" \
  tests

echo ">>> Saving backend logs"
docker compose logs backend > "$LOGS_DIR/backend.log" || true

echo ">>> API tests finished"