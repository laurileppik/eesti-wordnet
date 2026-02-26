#!/bin/sh
set -e

echo "Alusta db vastu ühenduse ootamist"

# Proovi pg_isready
if command -v pg_isready >/dev/null 2>&1; then
  until pg_isready -h db -p 5432 -U postgres >/dev/null 2>&1; do
    echo "pg_isready: waiting for db..."
    sleep 2
  done
  READY=1
fi

# Fallback1: bash /dev/tcp
if [ -z "$READY" ]; then
  if command -v bash >/dev/null 2>&1; then
    until bash -c 'cat < /dev/tcp/db/5432' >/dev/null 2>&1; do
      echo "bash /dev/tcp: waiting for db..."
      sleep 2
    done
    READY=1
  fi
fi

# Fallback2: nc
if [ -z "$READY" ]; then
  if command -v nc >/dev/null 2>&1; then
    until nc -z db 5432 >/dev/null 2>&1; do
      echo "nc: waiting for db..."
      sleep 2
    done
    READY=1
  fi
fi

echo "Postgres valmis"

if [ -z "$READY" ]; then
  echo "Ei leidnud masinast pg_isready, bashi /dev/tcp ega nc tööriista"
  sleep 5
fi

echo "Alusta jOOQ jaoks vajalike failide genereerimist"
if [ -d "/workspace/eewn-shared" ]; then
  cd /workspace/eewn-shared
elif [ -d "eewn-shared" ]; then
  cd eewn-shared
fi

if [ -f ./gradlew ]; then
  chmod +x ./gradlew || true
fi

if [ -x ./gradlew ]; then
  ./gradlew generateJooq --no-daemon
else
  gradle generateJooq --no-daemon
fi

echo "jOOQ generation tehtus"
