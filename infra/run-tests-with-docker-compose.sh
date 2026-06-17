#!/bin/bash

set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
HOST_PWD=$(pwd)
#была команда для винды сделала для убунту для ci агента


BASE_OUTPUT_DIR="./test-output/$TIMESTAMP"
LOGS_DIR="$BASE_OUTPUT_DIR/logs"
TARGET_DIR="${GITHUB_WORKSPACE}/target"


cleanup() {
  echo ">>> Stopping Docker Compose environment"
  docker compose down
}

# trap cleanup EXIT

echo ">>> Preparing output folders"

mkdir -p \
  "$LOGS_DIR" \
  "$TARGET_DIR/surefire-reports" \
  "$TARGET_DIR/surefire-report" \
  "$TARGET_DIR/allure-results" \
  "$TARGET_DIR/allure-report" \
  "$TARGET_DIR/swagger-coverage-output"

echo ">>> Pulling browser images"
docker pull selenoid/firefox:latest
docker pull selenoid/chrome:latest

echo ">>> Starting Docker Compose environment"
docker compose up -d backend
#frontend selenoid selenoid-ui

echo ">>> Waiting for environment to become ready"
sleep 20

echo "GITHUB_WORKSPACE: $GITHUB_WORKSPACE"
echo "TARGET_DIR: $TARGET_DIR"




#echo ">>> Running UI tests"
#TEST_PROFILE=ui docker compose run --rm \
#  -v "${HOST_PWD}/test-output/$TIMESTAMP/logs:/app/logs" \
#  -v "${TARGET_DIR}/surefire-reports:/app/target/surefire-reports" \
#  -v "${TARGET_DIR}/surefire-report:/app/target/site" \
#  -v "${TARGET_DIR}/allure-results:/app/target/allure-results" \
#  -v "${TARGET_DIR}/allure-report:/app/target/site/allure-maven-plugin" \
#  tests


echo "Before API:"
find ${TARGET_DIR}/allure-results -type f | wc -l

echo ">>> Running API tests"
TEST_PROFILE=api docker compose run --rm \
  -v "${HOST_PWD}/test-output/$TIMESTAMP/logs:/app/logs" \
  -v "${TARGET_DIR}/surefire-reports:/app/target/surefire-reports" \
  -v "${TARGET_DIR}/surefire-report:/app/target/site" \
  -v "${TARGET_DIR}/allure-results:/app/target/allure-results" \
  -v "${TARGET_DIR}/allure-report:/app/target/site/allure-maven-plugin" \
  -v "${TARGET_DIR}/swagger-coverage-output:/app/target/swagger-coverage-output" \
  tests

echo "After API:"
find ${TARGET_DIR}/allure-results -type f | wc -l


echo ">>> All tests finished"
echo "Logs: $LOGS_DIR"
echo "Target reports: $TARGET_DIR"