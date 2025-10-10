#!/bin/bash
set -e

host="db-tests"
port=5432

until pg_isready -h $host -p $port > /dev/null 2>&1; do
  echo "Esperando pelo Postgres em $host:$port..."
  sleep 2
done

echo "Postgres pronto! Iniciando aplicação..."
exec "$@"
