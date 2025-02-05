#!/bin/bash
echo "=== Starting backend deployment ==="

# 1. 构建JAR包
echo "Building JAR package..."
cd backend
mvn clean package -DskipTests
cd ..

# 2. 上传文件到服务器
echo "Uploading files to server..."
scp backend/target/backend-0.0.1-SNAPSHOT.jar lyl41368@182.92.67.26:/home/lyl41368/iot_project/backend/
scp backend/Dockerfile lyl41368@182.92.67.26:/home/lyl41368/iot_project/backend/
scp docker-compose.yml lyl41368@182.92.67.26:/home/lyl41368/iot_project/

# 3. 远程执行重启命令
echo "Restarting backend service..."
ssh -t lyl41368@182.92.67.26 "cd /home/lyl41368/iot_project && chmod +x restart-backend.sh && sudo ./restart-backend.sh"

echo "=== Deployment completed ===" 