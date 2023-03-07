FROM maven:3.8.6-openjdk-11 as builder
WORKDIR /code
ADD . /code
COPY base-env/settings.xml /usr/share/maven/conf/
#RUN mvn install:install-file -Dfile=/code/base-env/chainmaker-sdk-java-2.1.0.jar -DgroupId=org.chainmaker.sdk -DartifactId=chainmaker-sdk-java -Dversion=2.1.0 -Dpackaging=jar -DpomFile=/code/base-env/pom.xml
#RUN ["/usr/local/bin/mvn-entrypoint.sh","mvn","verify","clean","--fail-never"]
RUN mvn package

FROM maven:3.8.6-openjdk-11
WORKDIR /home/workspace
COPY --from=builder /code/target/parser-0.0.1-SNAPSHOT.jar /home/workspace/parser-0.0.1-SNAPSHOT.jar
CMD [ "java", "-jar", "parser-0.0.1-SNAPSHOT.jar" ]
