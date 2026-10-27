# ==============================================================================
# Multi-stage Dockerfile for EconWaAi Spring Boot Application
# Target: Java 21 LTS (Eclipse Temurin Alpine)
# ==============================================================================

# Build Stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Cache Maven wrapper and dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# Copy source code and build production artifact
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine AS runner
LABEL maintainer="EconWaAi Engineering <dev@econwa.ng>"

# Security: non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app
COPY --from=builder /workspace/target/EconWaAi-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Production JVM flags with Virtual Threads and container resource limits
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]