FROM eclipse-temurin:21-jdk-alpine

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} repository-api.jar

ENTRYPOINT ["java","-jar","/repository-api.jar"]
