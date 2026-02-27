# Stage 1: Build Frontend (Angular)
FROM node:20-alpine AS frontend-builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
# We should probably copy the shared proto definitions instead of leaving it out
COPY proto/ /proto/
RUN npm run build -- --configuration production

# Stage 2: Build Backend (Kotlin/Gradle)
FROM gradle:8.6-jdk21 AS backend-builder
WORKDIR /app
# We need the proto folder available during backend build as well
COPY proto/ /proto/
COPY backend/ ./
# Run the gradle build
RUN gradle shadowJar --no-daemon

# Stage 3: Final Image (Mongo base + JRE + Frontend + Backend)
FROM mongo:7.0-jammy
WORKDIR /app

# Install JRE 21
RUN apt-get update && \
    apt-get install -y openjdk-21-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy backend fat jar
COPY --from=backend-builder /app/build/libs/*-all.jar ./backend.jar

# Copy frontend static files
COPY --from=frontend-builder /app/dist/frontend/browser ./frontend-build

# Copy entrypoint script
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Expose Armeria Port
EXPOSE 8080

# Expose Mongo Port (Optional, if people want to connect directly)
EXPOSE 27017

# Volumes for Mongo and potential local storage
VOLUME [ "/data/db", "/tmp/holocron-assets" ]

ENTRYPOINT ["/app/entrypoint.sh"]
