#********程序打包镜像********
FROM maven:3.8.6-openjdk-11 as builder
WORKDIR /home/workspace
ADD . /home/workspace
COPY base-env/settings.xml /usr/share/maven/conf/
RUN mvn install:install-file -Dfile=/home/workspace/base-env/chainmaker-sdk-java-2.1.0.jar -DgroupId=org.chainmaker.sdk  \
    -DartifactId=chainmaker-sdk-java -Dversion=2.1.0 -Dpackaging=jar -DpomFile=/home/workspace/base-env/pom.xml
# RUN ["/usr/local/bin/mvn-entrypoint.sh","mvn","verify","clean","--fail-never"]
# RUN mvn install:install-file -Dfile=./lib/traffic-1.0.jar -DgropuId=com.baec -DartifactId=traffic-spring-boot-starter -Dversion=1.0 -Dpackaging=jar
RUN mvn package



#********程序执行镜像*********
#基础镜像
FROM openjdk:11.0.14.1-slim-buster
WORKDIR /home/workspace
COPY --from=builder /home/workspace/target/job-service-0.0.1-SNAPSHOT.jar /home/workspace/job-service-0.0.1-SNAPSHOT.jar
CMD [ "java", "-jar", "job-service-0.0.1-SNAPSHOT.jar" ]
