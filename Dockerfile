# jdk21 Image Start
FROM openjdk:21-jdk

# Working Directory Settings
WORKDIR /app

# Copy Environment Setting file
COPY src/main/resources/application.yml /app/application.yml
COPY src/main/resources/application-prod.yml /app/application-prod.yml

# Copy JAR File
COPY build/libs/Backend-0.0.1-SNAPSHOT.jar app.jar

# Starting Execute Commands
ENTRYPOINT ["java", "-jar", "app.jar"]
