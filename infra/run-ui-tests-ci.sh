#!/bin/bash

set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
HOST_PWD=$(pwd)

BASE_OUTPUT_DIR="./test-output/ui-$TIMESTAMP"

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

echo ">>> Preparing UI output folders"
mkdir -p "$LOGS_DIR" "$RESULTS_DIR" "$REPORT_DIR" "$SWAGGER_DIR" "$ALLURE_RESULTS_DIR"

echo ">>> Pulling browser images"
docker pull selenoid/firefox:latest
docker pull selenoid/chrome:latest

echo ">>> Building tests image"
docker compose build tests

echo ">>> Starting UI environment"
docker compose up -d backend frontend nginx selenoid selenoid-ui

echo ">>> Waiting for UI environment"
sleep 60

echo ">>> Running UI tests"
TEST_PROFILE=ui docker compose run --rm \
  -v "${HOST_PWD}/test-output/ui-$TIMESTAMP/logs:/app/logs" \
  -v "${HOST_PWD}/test-output/ui-$TIMESTAMP/results:/app/target/surefire-reports" \
  -v "${HOST_PWD}/test-output/ui-$TIMESTAMP/report:/app/target/site" \
  -v "${HOST_PWD}/test-output/ui-$TIMESTAMP/swagger-coverage-output:/app/target/swagger-coverage-output" \
  -v "${HOST_PWD}/test-output/ui-$TIMESTAMP/allure-results:/app/target/allure-results" \
  tests

echo ">>> Saving UI logs"
docker compose logs backend > "$LOGS_DIR/backend.log" || true
docker compose logs selenoid > "$LOGS_DIR/selenoid.log" || true
docker compose logs selenoid-ui > "$LOGS_DIR/selenoid-ui.log" || true

echo ">>> UI tests finished"