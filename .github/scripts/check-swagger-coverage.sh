#!/usr/bin/env bash
set -euo pipefail

REPORT_FILE="${1:-swagger-coverage-report.html}"
MIN_COVERAGE="${2:-5}"

if [[ ! -f "$REPORT_FILE" ]]; then
  echo "Coverage report not found: $REPORT_FILE"
  exit 1
fi

COVERAGE=$(grep -m1 -oP 'Full coverage:\s*\K[0-9]+(?:\.[0-9]+)?(?=%)' "$REPORT_FILE" || true)

if [[ -z "$COVERAGE" ]]; then
  echo "Could not parse API coverage from $REPORT_FILE"
  exit 1
fi

echo "API Full coverage: ${COVERAGE}%"
echo "Required minimum: ${MIN_COVERAGE}%"

awk -v actual="$COVERAGE" -v min="$MIN_COVERAGE" 'BEGIN { exit !(actual + 0 >= min + 0) }' \
  && echo "Quality gate passed" \
  || { echo "Quality gate failed"; exit 1; }