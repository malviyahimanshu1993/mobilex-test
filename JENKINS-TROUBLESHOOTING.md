# Jenkins Pipeline Troubleshooting Guide

## Issue: "package com.framework.pages does not exist"

### Root Cause
Jenkins pulls code from git repository, but your local changes (including new files like `SecurityMattersPage.java`) haven't been committed to git yet.

### Solution

**Step 1: Commit all files to git**
```bash
git add -A
git commit -m "Add all framework enhancements and pages"
git push origin main  # or your branch name
```

**Step 2: Rebuild Docker image without cache**
In Jenkins, the pipeline now automatically does `docker build --no-cache` to ensure fresh builds.

**Step 3: Re-run Jenkins pipeline**
- Go to Jenkins > Your Job
- Click "Build with Parameters"
- Select `ENV=docker` (or `local`)
- Click "Build"

---

## Common Jenkins Pipeline Failures

### 1. "Docker: command not found"
**Cause**: Docker not installed or not in PATH  
**Fix**: 
- Install Docker on Jenkins agent
- Add docker to PATH: `C:\Program Files\Docker\Docker\resources\bin`
- Restart Jenkins service

### 2. "Appium Server is not reachable"
**Cause**: Appium not running on host  
**Fix**:
- Start Appium: `appium` or `npx appium`
- Verify: `curl http://127.0.0.1:4723/status`
- Check Jenkins console for exact error

### 3. "Permission denied: Docker"
**Cause**: Jenkins user can't run docker  
**Fix** (Windows):
- Run Jenkins as Administrator
- Or add Jenkins user to docker-users group

### 4. "Device not found"
**Cause**: Android device/emulator not connected  
**Fix**:
- Check: `adb devices`
- For real device: Enable USB debugging, check cable
- For emulator: Start emulator first, then run tests

### 5. "Build cache issues"
**Symptoms**: Tests pass locally but fail in Docker  
**Fix**: Pipeline now includes `docker build --no-cache` to force clean builds

---

## Debugging Steps

### Check what files are in Jenkins workspace:
```groovy
// In Jenkinsfile stage
bat '''
    echo Listing Java source files:
    dir src\\test\\java\\com\\framework\\pages\\
    dir src\\test\\java\\com\\framework\\tests\\
    dir src\\test\\java\\com\\framework\\base\\
'''
```

### Check what gets copied to Docker:
```dockerfile
# In Dockerfile
RUN ls -la src/test/java/com/framework/pages/ || echo "Missing pages"
RUN find src -name "*.java" | head -20
```

### Test locally first:
```bash
# Local test (before committing)
mvn clean test -Denv=local

# Local Docker test
.\scripts\run-docker.ps1

# Then commit if successful
git add -A && git commit -m "Message" && git push
```

---

## Pre-Pipeline Checklist

- [ ] All Java files created/modified
- [ ] Files are in correct package structure
- [ ] All files committed to git: `git status` shows clean
- [ ] Appium running: `curl http://127.0.0.1:4723/status`
- [ ] Device connected: `adb devices` shows device
- [ ] Local tests pass: `mvn test -Denv=local`
- [ ] Docker builds: `.\scripts\run-docker.ps1`

---

## Git Commands for Jenkins

```bash
# Check git status
git status

# Add all changes
git add -A

# Commit with message
git commit -m "Add framework enhancements"

# Push to repository (Jenkins will pull from here)
git push origin main

# Verify files on remote
git ls-tree -r HEAD src/test/java/com/framework/
```

---

## Quick Fix Checklist

If Jenkins fails:

1. **Commit everything to git**
   ```bash
   git add -A && git commit -m "All changes" && git push
   ```

2. **Clean Jenkins workspace**
   - Jenkins > Job > "Delete workspace"
   - Rebuild job

3. **Force Docker rebuild**
   - Pipeline already uses `--no-cache`
   - Or manually: `docker rmi -f mobilex-test:ci`

4. **Check Appium**
   ```bash
   # On Windows host
   appium
   # Or in new terminal
   curl http://127.0.0.1:4723/status
   ```

5. **Verify device**
   ```bash
   adb devices
   ```

6. **Re-run pipeline**
   - Select ENV=docker
   - Click Build

---

## Emergency Debug: Manual Docker Run

If Jenkins pipeline fails, debug manually:

```powershell
# Build image with debug output
docker build --no-cache -t mobilex-test:debug .

# Run with interactive shell to see errors
docker run -it --rm `
    --add-host=host.docker.internal:host-gateway `
    -e ENV=docker `
    -v "${PWD}\target:/workspace/target" `
    mobilex-test:debug bash

# Inside container:
ls -la src/test/java/com/framework/pages/
mvn compile test-compile
```

---

## What Files Should Exist

Your git repository should have these files:

```
src/test/java/com/framework/
├── base/
│   ├── BaseTest.java           ✓ Should exist
│   ├── BasePage.java           ✓ Should exist
│   └── DriverManager.java      ✓ Should exist
├── pages/
│   └── SecurityMattersPage.java ✓ CRITICAL - Missing if tests fail
├── tests/
│   └── SecurityMattersTest.java ✓ Should exist
└── config/
    └── Config.java             ✓ Should exist
```

If any of these files are missing from Jenkins workspace, commit them:
```bash
git add src/test/java/com/framework/pages/SecurityMattersPage.java
git commit -m "Add SecurityMattersPage"
git push
```

