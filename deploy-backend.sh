#!/bin/bash
echo "=== Starting backend deployment ==="

# 1. 构建JAR包
echo "Building JAR package..."
cd backend
mvn clean package -DskipTests
cd ..

# 2. 上传新文件
echo "Uploading files to server..."
scp backend/target/backend-0.0.1-SNAPSHOT.jar lyl41368@182.92.67.26:/home/lyl41368/iot_project/backend/
scp backend/Dockerfile lyl41368@182.92.67.26:/home/lyl41368/iot_project/backend/
scp docker-compose.yml lyl41368@182.92.67.26:/home/lyl41368/iot_project/

# 3. 执行远程命令（使用单个SSH连接）
echo "Deploying on server..."
ssh -t lyl41368@182.92.67.26 "cd /home/lyl41368/iot_project && \
    sudo docker stop iot-backend && \
    sudo docker rm iot-backend && \
    sudo docker-compose up -d backend"

echo "=== Deployment completed ===" 