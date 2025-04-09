FROM amazoncorretto:21
LABEL authors="alexandergorobets"

COPY target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8001"
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
ENTRYPOINT ["java", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8001", "app.jar"]
