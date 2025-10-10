# Etapa 1: build
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon --stacktrace --info

# Etapa 2: runtime
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Copiar o script de wait-for-postgres
COPY ./modules/repository_jdbi/tests/scripts/wait-for-postgres.sh ./bin/wait-for-postgres.sh
RUN chmod +x ./bin/wait-for-postgres.sh

# Iniciar a app só depois de o Postgres estar disponível
ENTRYPOINT ["./bin/wait-for-postgres.sh", "java", "-jar", "app.jar"]
EXPOSE 8080
