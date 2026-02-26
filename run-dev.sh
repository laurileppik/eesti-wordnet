#!/usr/bin/env bash
set -euo pipefail

echo "Käivita Postgres container"
docker compose up -d db

echo "Jooksuta codegen container"
docker compose run --rm codegen

docker compose build backend
docker compose up -d backend frontend

echo "Rakendus peaks avanema:"
echo "http://localhost:80"
exit 0
