#!/bin/bash
set -e

cd /home/ec2-user/app

echo "Pulling latest source..."
git pull origin main

echo "Building and starting services..."
docker compose -f docker/docker-compose.prod.yml --env-file .env up --build -d --remove-orphans

echo "Removing unused images..."
docker image prune -f

echo "Deploy completed."
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
