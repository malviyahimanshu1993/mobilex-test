# Jenkins + Docker (containerized) execution for this project

This project is a Maven + TestNG + Appium (Java client) test suite.

## What “containerized” means here
- The **test runner** (Maven/TestNG) runs in a Docker container.
- The **Appium server** can run in a container.
- The **Android device/emulator** usually **cannot** be reasonably containerized for CI on Windows; it typically runs:
  - on the Jenkins agent (Android Emulator), or
  - as a dedicated device farm (BrowserStack/Sauce Labs), or
  - on Linux with KVM-based Android emulator containers (complex).

## Files added
- `Dockerfile`: builds a Maven image and runs `mvn test`.
- `docker-compose.yml`: starts Appium + test runner.
- `Jenkinsfile`: pipeline that builds image, runs compose, archives results, publishes Allure.

## Jenkins prerequisites
1. Jenkins agent has Docker installed and the Jenkins user can run docker.
2. Install Jenkins plugins:
   - Pipeline
   - Docker Pipeline (optional)
   - Allure (optional but recommended)
3. Configure Allure (if using plugin): Global Tool Configuration → Allure.

## How the pipeline works
- Builds test image: `docker build -t mobilex-test:ci .`
- Runs compose: `docker compose up --abort-on-container-exit --exit-code-from tests`
  - Appium listens on `:4723`
  - Test container runs Maven tests and writes `target/allure-results`
- Jenkins archives Surefire + Allure results.

## Integration detail (important)
Your Java test framework must read the Appium URL from `APPIUM_SERVER_URL` env var (or system property).
If it is hardcoded to `http://127.0.0.1:4723`, it will fail inside Docker.

## Typical difficulties / gotchas
### 1) Emulator/device access from containers
- **Windows Docker Desktop** can’t reliably expose USB Android devices into Linux containers.
- Running an Android Emulator inside Docker is generally Linux/KVM-only.
**Recommendation**: run the emulator on the Jenkins agent (outside Docker) and point the test container to it.

### 2) Networking: localhost is not the same inside containers
- Inside `tests` container, `localhost` refers to the container itself.
- Use `http://appium:4723` (compose service name) or pass host gateway.

### 3) ADB is required for Appium + device
- Appium must be able to run `adb devices` and see the device.
- You may need Android platform tools on the Appium container/host.

### 4) Privileged mode (Linux)
- Real device via USB usually needs:
  - `privileged: true`
  - `-v /dev/bus/usb:/dev/bus/usb`
- Not portable to Windows agents.

### 5) Performance and stability
- Emulators are slow; CI needs enough CPU/RAM.
- Flaky tests increase under resource pressure.

### 6) Allure results persistence
- Always mount `./target` from host so Jenkins can archive results.

## Recommended CI architecture
### Option A (most common)
- Jenkins agent runs Android Emulator (or connected device) + Appium on the agent.
- Docker runs only Maven tests.

### Option B
- Use Sauce Labs/BrowserStack → tests container only needs internet access.

### Option C (advanced)
- Linux Jenkins agents + KVM and an Android emulator container stack.

# Runs Maven tests inside a container.
# NOTE: This image only runs the Java/TestNG code. It does NOT provide an Android emulator.
# You must run Appium + device/emulator separately (e.g., on the Jenkins agent, or via a Selenium Grid/Appium device farm).

FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /workspace

# Cache deps first
COPY pom.xml .
RUN mvn -q -DskipTests=true dependency:go-offline

# Copy sources
COPY src ./src
COPY scripts ./scripts
COPY bundle-to-test ./bundle-to-test

# Default Appium endpoint; override in docker-compose/Jenkins
ENV APPIUM_SERVER_URL=http://appium:4723

# Run tests
CMD ["mvn", "-q", "test"]

