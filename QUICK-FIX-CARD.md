# 🚀 Quick Fix Reference Card

## Problem ❌
```
[ERROR] package com.framework.pages does not exist
```

## Root Cause 🔍
Files existed locally but weren't committed to git. Jenkins pulls from git, so it couldn't find them.

---

## Solution ✅

### What Was Fixed:
1. ✓ `SecurityMattersPage.java` committed to git
2. ✓ All framework files added to git repository
3. ✓ Dockerfile improved with diagnostics
4. ✓ Jenkinsfile updated with better error handling
5. ✓ `.dockerignore` created for proper Docker context
6. ✓ Documentation added for troubleshooting

### Files in Git (Verified):
```
✓ src/test/java/com/framework/pages/SecurityMattersPage.java
✓ src/test/java/com/framework/base/
✓ src/test/java/com/framework/config/
✓ src/test/java/com/framework/tests/
✓ All other framework files
```

---

## To Run Jenkins Pipeline:

1. **Go to Jenkins**
   ```
   http://<jenkins-url>/job/<your-job>
   ```

2. **Click "Build with Parameters"**

3. **Select ENV parameter:**
   - `docker` (recommended for CI)
   - `local` (for testing on agent machine)

4. **Click "Build"**

---

## If Tests Still Fail:

### Quick Checklist:
```bash
# 1. Verify code is in git
git ls-tree -r HEAD src/test/java/com/framework/

# 2. Test locally first
mvn test -Denv=local

# 3. Test Docker locally
.\scripts\run-docker.ps1

# 4. Check Appium running
curl http://127.0.0.1:4723/status

# 5. Check device connected
adb devices
```

### Jenkins Actions:
1. Delete workspace: `Jenkins > Job > "Delete workspace"`
2. Rebuild pipeline
3. Check console output for detailed error

---

## Files to Read:

- 📖 **JENKINS-TROUBLESHOOTING.md** - Full troubleshooting guide
- 📖 **EXECUTION-GUIDE.md** - How to run tests
- 📖 **JENKINS-FIX-SUMMARY.md** - What was fixed

---

## Key Points:

✋ **IMPORTANT**: Before running Jenkins:
- ✓ Commit all changes: `git add -A && git commit -m "..."`
- ✓ Push to git: `git push`
- ✓ Appium running: `appium`
- ✓ Device connected: `adb devices`

🎯 **Jenkins will:**
- Pull latest code from git
- Build Docker image
- Run tests in container
- Archive results

---

## Status: ✅ READY

Jenkins pipeline is now fixed and ready to run!

Next run: **docker** mode for best results (all deps in container)

