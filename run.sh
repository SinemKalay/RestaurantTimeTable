#!/bin/bash

./gradlew build jibDockerBuild

docker tag restaurant:0.0.1-SNAPSHOT esineka/restaurant:latest

## This line below is required to log in "esineka" dockerHub account
## It will push latest changes to hub.
#  docker push esineka/restaurant

docker-compose up -d
