FROM openjdk:22-jdk
ARG JAR_FILE=target/api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} agrotech.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/agrotech.jar"]