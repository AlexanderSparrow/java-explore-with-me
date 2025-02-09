#FROM ubuntu:latest
FROM eclipse-temurin:21-jre-jammy
LABEL authors="alexandergorobets"
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["top", "-b"]
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]