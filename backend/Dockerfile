# Stage 1: Build
FROM gradle:8.6-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew installDist --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/install/backend .
EXPOSE 8080
ENV MONGODB_URI=""
ENV MONGODB_DATABASE="holocron"
CMD ["bin/backend"]
