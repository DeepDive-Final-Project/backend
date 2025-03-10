# jdk21 Image Start
FROM openjdk:21-jdk

# Working Directory Settings
WORKDIR /app

# JAR 파일 복사
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# Starting Execute Commands
ENTRYPOINT ["java", "-jar", "app.jar"]
