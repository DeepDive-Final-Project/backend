# jdk21 Image Start
FROM openjdk:21-jdk

# Working Directory Settings
WORKDIR /app

# jar File Copy
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Starting Execute Commands
ENTRYPOINT ["java", "-jar", "app.jar"]
