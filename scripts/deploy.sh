#!/bin/bash
set -e

cd /home/ec2-user/app

# JSON 배열을 공백 구분 문자열로 변환 (예: ["member-service","order-service"] → member-service order-service)
SERVICES=$(echo "$1" | tr -d '[]"' | tr ',' ' ')

echo "Deploying services: $SERVICES"

echo "Pulling latest images..."
docker compose -f docker/docker-compose.prod.yml --env-file .env pull $SERVICES

echo "Restarting changed services..."
docker compose -f docker/docker-compose.prod.yml --env-file .env up -d --no-deps $SERVICES

echo "Removing unused images..."
docker image prune -f

echo "Deploy completed."
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
