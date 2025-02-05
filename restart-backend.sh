#!/bin/bash
echo "Stopping backend service..."
docker stop iot-backend

echo "Removing backend container..."
docker rm iot-backend

echo "Starting backend service..."
docker-compose up -d backend

echo "Showing logs..."
docker logs -f iot-backend 