FROM amazoncorretto:21
LABEL authors="alexandergorobets"

COPY target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
