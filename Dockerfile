# Etapa 1: build
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon --stacktrace --info

# Etapa 2: runtime
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
