# Etapa 1: build
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon --stacktrace --info

# Etapa 2: runtime
FROM openjdk:21-jdk-slim
WORKDIR /app

# Instalar cliente PostgreSQL ?? sera necessiario ?
RUN apt-get update && apt-get install -y postgresql-client

# Copiar o jar e script
COPY --from=build /app/modules/host/build/libs/host-1.0-SNAPSHOT.jar app.jar
COPY ./modules/repository_jdbi/tests/scripts/wait-for-postgres.sh ./bin/wait-for-postgres.sh
RUN chmod +x ./bin/wait-for-postgres.sh

# Iniciar a app só depois de o Postgres estar disponível usando o scrpit
ENTRYPOINT ["./bin/wait-for-postgres.sh", "java", "-jar", "app.jar"]
EXPOSE 8080
