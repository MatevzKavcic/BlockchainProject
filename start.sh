#!/bin/bash


pwd

cd Blockchain 

mvn clean package

cd ..\\BlockchainNode

mvn clean package 

cd ..


COMPOSE_FILE="docker-compose.yml"
SERVICES=("mainnode" "normalnode1" "normalnode2" "normalnode3" "normalnode4") # "normalnode5"
 #"normalnode6" "normalnode7" "normalnode8" "normalnode9" "normalnode10")
  # "normalnode11" "normalnode12" "normalnode13" "normalnode14" "normalnode15")
  #  "normalnode16" 
# "normalnode17" "normalnode18" "normalnode19" "normalnode20")
DELAY=5  # Time in seconds between starting each service

for SERVICE in "${SERVICES[@]}"; do
  echo "Starting $SERVICE..."
  docker-compose -f $COMPOSE_FILE up -d $SERVICE
  echo "$SERVICE started. Waiting for $DELAY seconds..."
  sleep $DELAY
done

echo "All services started."
docker ps
