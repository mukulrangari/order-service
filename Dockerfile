# FROM openjdk:12-jdk-alpine
# VOLUME /tmp
# ARG JAR_FILE
# COPY ${JAR_FILE} app.jar
# ENTRYPOINT ["java", "-jar", "/app.jar"]

FROM  openjdk:17-oracle
ADD build/libs/* order-service-0.0.1-SNAPSHOT.jar
EXPOSE 8081
ENTRYPOINT [ "java", "-jar", "order-service-0.0.1-SNAPSHOT.jar"]