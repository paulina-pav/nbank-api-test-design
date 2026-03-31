#!/bin/bash

set -e

echo ">>> Остановить Docker Compose"
docker compose down

echo ">>> Docker pull все образы браузеров"
echo "Pulling selenoid/firefox:latest..."
docker pull selenoid/firefox:latest

echo "Pulling selenoid/chrome:latest..."
docker pull selenoid/chrome:latest

echo ">>> Запуск Docker Compose"
docker compose up -d backend frontend nginx selenoid selenoid-ui
#docker compose up
