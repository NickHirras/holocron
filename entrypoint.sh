#!/bin/bash
set -e

# Start MongoDB if MONGODB_URI is not set to an external address
if [ -z "$MONGODB_URI" ] || [[ "$MONGODB_URI" == *"localhost"* ]] || [[ "$MONGODB_URI" == *"127.0.0.1"* ]]; then
    echo "Starting internal MongoDB..."
    # Ensure MongoDB data directory exists and has correct permissions
    mkdir -p /data/db
    chown -R mongodb:mongodb /data/db
    # Start mongod in background. The image has gosu installed so we can run as mongodb
    gosu mongodb mongod > /var/log/mongodb.log 2>&1 &
    
    # Wait a few seconds for Mongo to boot
    sleep 3
    echo "MongoDB started internally."
else
    echo "Using external MongoDB at: $MONGODB_URI"
fi

# Determine path to internal frontend-build
FRONTEND_PATH="/app/frontend-build"
if [ ! -d "$FRONTEND_PATH" ]; then
    echo "Warning: Frontend build not found at $FRONTEND_PATH"
fi

echo "Starting Backend API..."
# Start the Kotlin application (which also serves the frontend from frontend-build directory via armeria config)
cd /app
exec java -jar backend.jar
