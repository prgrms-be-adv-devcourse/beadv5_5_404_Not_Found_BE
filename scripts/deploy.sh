#!/bin/bash
set -e

cd /home/ubuntu/app

echo "Pulling latest images..."
docker compose -f docker/docker-compose.prod.yml pull

echo "Starting services..."
docker compose -f docker/docker-compose.prod.yml up -d --remove-orphans

echo "Removing unused images..."
docker image prune -f

echo "Deploy completed."
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
