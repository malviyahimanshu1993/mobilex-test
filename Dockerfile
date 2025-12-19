FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /workspace

# Cache dependencies first for faster rebuilds
COPY pom.xml .
RUN mvn -q -DskipTests=true dependency:go-offline

# Copy project sources
COPY src ./src
COPY scripts ./scripts
COPY bundle-to-test ./bundle-to-test

# Default Appium endpoint; override in docker-compose/Jenkins
ENV APPIUM_SERVER_URL=http://appium:4723

CMD ["mvn", "-q", "test"]
