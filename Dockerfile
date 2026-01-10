FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /workspace

# Cache dependencies first for faster rebuilds
COPY pom.xml .
RUN mvn -q -DskipTests=true dependency:go-offline

# Copy project sources
COPY src ./src
COPY scripts ./scripts
COPY bundle-to-test ./bundle-to-test

# Environment configuration for Docker execution
ENV ENV=docker
ENV APPIUM_SERVER_URL=http://host.docker.internal:4723

# Default command runs tests in docker mode
CMD ["mvn", "test", "-Denv=docker"]
