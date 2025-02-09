#FROM ubuntu:latest
#FROM eclipse-temurin:21-jre-jammy
#LABEL authors="alexandergorobets"
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["top", "-b"]
#ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
FROM amazoncorretto:21
LABEL authors="alexandergorobets"
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9090
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]