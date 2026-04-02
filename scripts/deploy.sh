#!/bin/bash
set -e

cd /home/ec2-user/app

# JSON 배열을 공백 구분 문자열로 변환 (예: ["member-service","order-service"] → member-service order-service)
SERVICES=$(echo "$1" | tr -d '[]"' | tr ',' ' ')

echo "Deploying services: $SERVICES"

# systemd 서비스 등록 (최초 1회)
if [ ! -f /etc/systemd/system/bookcommerce.service ]; then
  echo "Registering systemd service..."
  sudo tee /etc/systemd/system/bookcommerce.service > /dev/null << 'SYSTEMD'
[Unit]
Description=BookCommerce Docker Compose
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ec2-user/app/docker
ExecStart=/usr/bin/docker compose -f docker-compose.prod.yml up -d
ExecStop=/usr/bin/docker compose -f docker-compose.prod.yml down
User=ec2-user

[Install]
WantedBy=multi-user.target
SYSTEMD
  sudo systemctl daemon-reload
  sudo systemctl enable bookcommerce.service
  echo "systemd service registered."
fi

echo "Pulling latest images..."
docker compose -f docker/docker-compose.prod.yml --env-file .env pull $SERVICES

echo "Restarting changed services..."
docker compose -f docker/docker-compose.prod.yml --env-file .env up -d --no-deps $SERVICES

echo "Removing unused images..."
docker image prune -f

echo "Deploy completed."
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
