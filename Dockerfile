FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /workspace

# Cache dependencies first for faster rebuilds
COPY pom.xml .
RUN mvn -q -DskipTests=true dependency:go-offline

# Copy entire source tree - explicitly
COPY src ./src
COPY scripts ./scripts
COPY bundle-to-test ./bundle-to-test

# Verify files are present
RUN ls -la src/test/java/com/framework/pages/ || echo "WARNING: pages directory not found"
RUN ls -la src/test/java/com/framework/tests/ || echo "WARNING: tests directory not found"

# Environment configuration for Docker execution
ENV ENV=docker
ENV APPIUM_SERVER_URL=http://host.docker.internal:4723

# Default command runs tests in docker mode
CMD ["mvn", "test", "-Denv=docker"]
