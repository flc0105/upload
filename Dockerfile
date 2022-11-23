FROM openjdk:8-jdk-alpine
WORKDIR /root/upload
COPY target/*.jar app.jar
COPY target/upload.db .
ENV PORT 80
ENV PASSWORD flc
ENTRYPOINT java -jar app.jar --server.port=$PORT --password=$PASSWORD
