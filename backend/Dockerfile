# 使用JDK 17作为基础镜像
FROM openjdk:17-jdk-slim

# 工作目录
WORKDIR /app

# 复制JAR文件
COPY backend-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java","-jar","app.jar"] 