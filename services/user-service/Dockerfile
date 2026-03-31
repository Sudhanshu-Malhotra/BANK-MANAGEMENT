# ============================================
# Stage 1: Build
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY infrastructure infrastructure
COPY services services

RUN chmod +x gradlew && \
    ./gradlew :services:user-service:build -x test --no-daemon --parallel

# ============================================
# Stage 2: Runtime (minimal image)
# ============================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security (CIS Docker Benchmark)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/services/user-service/build/libs/*-SNAPSHOT.jar app.jar

# Set appropriate file ownership
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8081

# JVM tuning for containerized environments
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", \
  "-jar", "app.jar"]
