# jdk21 Image Start
FROM openjdk:21-jdk

# Working Directory Settings
WORKDIR /app

# JAR 파일 복사
COPY build/libs/Backend-0.0.1-SNAPSHOT.jar app.jar

# Starting Execute Commands
ENTRYPOINT ["java", "-jar", "app.jar"]
