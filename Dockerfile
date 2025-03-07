# jdk21 Image Start
FROM openjdk:21-jdk

# Working Directory Settings
WORKDIR /app

# jar File Copy
ARG JAR_FILE=Backend-0.0.1-SNAPSHOT.jar
COPY build/libs/${JAR_FILE} app.jar

# Starting Execute Commands
ENTRYPOINT ["java", "-jar", "app.jar"]
