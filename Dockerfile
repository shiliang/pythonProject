#FROM maven:3.8.6-openjdk-11 as builder
FROM docker.oa.com:5000/job-service-base:v1 as builder
WORKDIR /code
ADD . /code
COPY base-env/settings.xml /usr/share/maven/conf/
#RUN mvn install:install-file -Dfile=/code/base-env/chainmaker-sdk-java-2.1.0.jar -DgroupId=org.chainmaker.sdk -DartifactId=chainmaker-sdk-java -Dversion=2.1.0 -Dpackaging=jar -DpomFile=/code/base-env/pom.xml
#RUN ["/usr/local/bin/mvn-entrypoint.sh","mvn","verify","clean","--fail-never"]
RUN mvn clean package

FROM maven:3.8.6-openjdk-11
WORKDIR /home/workspace
COPY --from=builder /code/target/job-service-0.0.1-SNAPSHOT.jar /home/workspace/job-service-0.0.1-SNAPSHOT.jar
CMD [ "java", "-jar", "job-service-0.0.1-SNAPSHOT.jar" ]
