#!/bin/bash

COMPOSE_FILE="docker-compose.yml"
SERVICES=("mainnode" "normalnode1" "normalnode2" "normalnode3" "normalnode4" "normalnode5")
DELAY=10  # Time in seconds between starting each service

for SERVICE in "${SERVICES[@]}"; do
  echo "Starting $SERVICE..."
  docker-compose -f $COMPOSE_FILE up -d $SERVICE
  echo "$SERVICE started. Waiting for $DELAY seconds..."
  sleep $DELAY
done

echo "All services started."
docker ps
