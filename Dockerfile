# 第一阶段：构建阶段
FROM maven:3.8.6-openjdk-11 AS builder
WORKDIR /home/workspace
# 复制项目文件
ADD . /home/workspace
# 构建项目，跳过测试
RUN mvn package -Dmaven.test.skip=true

# 第二阶段：运行镜像
FROM openjdk:11.0.14.1-slim-buster
WORKDIR /home/workspace
# 设置环境变量
ARG JAR_VERSION=1.1.0-SNAPSHOT
# 复制构建的 JAR 文件
COPY --from=builder /home/workspace/target/mira-job-service-${JAR_VERSION}.jar /home/workspace/mira-job-service.jar
# 启动应用
CMD ["java", "-jar", "mira-job-service.jar"]
