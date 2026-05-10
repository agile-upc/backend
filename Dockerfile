FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/api-1.0.0.jar
COPY ${JAR_FILE} agrotech.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/agrotech.jar"]
