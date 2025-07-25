# Multi-stage Dockerfile for MOBA Authorization Service

# Build stage
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
USER quarkus
WORKDIR /code

# Copy pom.xml first for better layer caching
COPY --chown=quarkus:quarkus pom.xml /code/

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -q

# Copy source code
COPY --chown=quarkus:quarkus src /code/src

# Build the application
RUN mvn package -Dnative -DskipTests

# Runtime stage - Minimal container for native binary
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/

# Copy the native binary
COPY --from=build /code/target/*-runner /work/application

# Set up non-root user
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
USER 1001

# Expose the port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/q/health || exit 1

# Run the application
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"] 