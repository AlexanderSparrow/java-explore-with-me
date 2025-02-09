#FROM eclipse-temurin:21-jre-jammy
FROM openjdk:17-jdk-alpine
LABEL authors="alexandergorobets"
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]