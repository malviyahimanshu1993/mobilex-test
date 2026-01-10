# MobileX Test Framework - Execution Guide

## Quick Start

### 🏠 Local Execution
Run tests on your local machine with Appium running at `http://127.0.0.1:4723`:

```bash
# Using Maven profile
mvn test -Plocal

# Using -Denv flag
mvn test -Denv=local

# Using PowerShell script
.\scripts\run-local.ps1
```

### 🐳 Docker Execution
Run tests in Docker container (Appium on host at `host.docker.internal:4723`):

```bash
# Using Maven profile
mvn test -Pdocker

# Using -Denv flag
mvn test -Denv=docker

# Using PowerShell script
.\scripts\run-docker.ps1

# Direct Docker run
docker build -t mobilex-test:local .
docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    -e ENV=docker \
    -v "${PWD}/target:/workspace/target" \
    mobilex-test:local
```

### 🔧 Jenkins Pipeline
The Jenkinsfile supports both modes via a parameter:

```groovy
// Pipeline parameter: ENV (docker or local)
// Docker mode: Builds image and runs tests in container
// Local mode: Runs tests directly on Jenkins agent
```

---

## Configuration

### Environment Flag (`-Denv`)

| Value | Appium URL | Use Case |
|-------|-----------|----------|
| `local` | `http://127.0.0.1:4723` | Development, local testing |
| `docker` | `http://host.docker.internal:4723` | Docker, Jenkins CI |

### Priority Order
Configuration values are resolved in this order:
1. Environment variable (e.g., `APPIUM_SERVER_URL`)
2. System property (e.g., `-DappiumServerUrl=...`)
3. Maven profile (e.g., `-Plocal` or `-Pdocker`)
4. `config.properties` file
5. Default value

### Maven Profiles

| Profile | Description |
|---------|-------------|
| `local` (default) | Local execution with `http://127.0.0.1:4723` |
| `docker` | Docker/CI execution with `http://host.docker.internal:4723` |

---

## Examples

### Run specific test class locally
```bash
mvn test -Denv=local -Dtest=SecurityMattersTest
```

### Run with custom device
```bash
mvn test -Denv=local -DdeviceName=Pixel_6 -Dudid=emulator-5554
```

### Run in Docker with custom Appium URL
```bash
docker run --rm \
    -e APPIUM_SERVER_URL=http://192.168.1.100:4723 \
    -v "${PWD}/target:/workspace/target" \
    mobilex-test:local
```

### Jenkins with parameters
```groovy
// In Jenkins, select ENV parameter:
// - docker: Tests run in container, connect to host Appium
// - local: Tests run directly on agent with local Appium
```

---

## Prerequisites

### Local Execution
1. Java 17+
2. Maven 3.6+
3. Appium 2.x running (`appium` or `npx appium`)
4. Android device/emulator connected (`adb devices`)

### Docker Execution
1. Docker installed
2. Appium 2.x running on host machine
3. Android device/emulator connected to host
4. Host Appium accessible from container via `host.docker.internal`

---

## Troubleshooting

### "Driver is null" error
- Ensure Appium is running at the correct URL
- Check if device is connected: `adb devices`
- Verify env flag matches your setup

### Docker can't reach Appium
- On Windows/Mac: `host.docker.internal` should work automatically
- On Linux: Add `--add-host=host.docker.internal:host-gateway` to docker run

### Appium not finding device
- Ensure `adb` is in PATH where Appium runs
- Check USB debugging is enabled on device
- Run `adb devices` to verify connection

---

## File Structure

```
├── scripts/
│   ├── run-local.ps1      # PowerShell script for local execution
│   └── run-docker.ps1     # PowerShell script for Docker execution
├── Dockerfile             # Docker image definition
├── docker-compose.yml     # Docker Compose configuration
├── Jenkinsfile           # Jenkins pipeline with env parameter
├── pom.xml               # Maven profiles: local, docker
└── src/test/resources/
    └── config.properties  # Default configuration
```

