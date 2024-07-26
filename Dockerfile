#********程序打包镜像********
FROM maven:3.8.6-openjdk-11 as builder
WORKDIR /home/workspace
ADD . /home/workspace
# 使用国内代理
RUN mkdir ~/.m2 && cp settings.xml ~/.m2/
# RUN mvn clean package
RUN mvn package -Dmaven.test.skip=true

#********程序执行镜像*********
#基础镜像
FROM openjdk:11.0.14.1-slim-buster
WORKDIR /home/workspace
COPY --from=builder /home/workspace/target/mira-job-service-1.1.0-SNAPSHOT.jar /home/workspace/mira-job-service-1.1.0-SNAPSHOT.jar
CMD [ "java", "-jar", "mira-job-service-1.1.0-SNAPSHOT.jar" ]
