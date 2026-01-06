#!/bin/bash

# Ensure Java 21 is on PATH for Maven/Lombok compatibility
if [ -z "$JAVA_HOME" ] && [ -d "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" ]; then
  export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "JAVA_HOME set to $JAVA_HOME"
fi

# Use a repo-local Maven cache to avoid macOS home-directory restrictions
MAVEN_LOCAL_REPO="${MAVEN_LOCAL_REPO:-$PWD/.m2-cache}"
mkdir -p "$MAVEN_LOCAL_REPO"
echo "Using Maven local repository at $MAVEN_LOCAL_REPO"

# Stop any running docker-compose services
docker-compose down || true
# Stop any running Docker containers and remove them
docker stop $(docker ps -q) || true
docker rm $(docker ps -aq) || true

# Kill all running Java processes before starting services
pkill -f java || true
  sleep 5

echo "========================================="
echo "Building microservices Docker images..."
echo "========================================="

# Build all the Docker images using Maven package goal
mvn -Dmaven.repo.local="$MAVEN_LOCAL_REPO" clean package -DskipTests -Ddocker.skip=true

# Check if the Maven build was successful
if [ $? -ne 0 ]; then
  echo "Maven build failed. Aborting Docker Compose startup."
  exit 1
fi

echo "========================================="
echo "Starting Docker Compose environment..."
echo "========================================="

# Start the Docker Compose environment
docker-compose up -d

echo "========================================="
echo "Services are starting up..."
echo "========================================="
echo "You can check the status with: docker-compose ps"
echo "View logs with: docker-compose logs -f [service_name]"
