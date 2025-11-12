# ===================================================================
# Multi-stage Dockerfile para Padel Club Pro
# ===================================================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
ARG SPRING_PROFILE=production
RUN mvn clean package -DskipTests -Pproduction \
    && mv target/*.jar target/padelclub-pro.jar

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install dependencies
RUN apk add --no-cache \
    curl \
    tzdata \
    && cp /usr/share/zoneinfo/Europe/Madrid /etc/localtime \
    && echo "Europe/Madrid" > /etc/timezone \
    && apk del tzdata

# Create non-root user
RUN addgroup -g 1000 padel && \
    adduser -D -u 1000 -G padel padel

# Copy JAR from build stage
COPY --from=build /app/target/padelclub-pro.jar /app/padelclub-pro.jar

# Create directories
RUN mkdir -p /app/logs /app/data && \
    chown -R padel:padel /app

# Switch to non-root user
USER padel

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/padelclub-pro.jar"]
