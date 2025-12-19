package com.framework.base;

import com.framework.config.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Starts/stops a local Appium server programmatically (optional).
 * <p>
 * Behavior:
 * - If config.startLocalAppium() is false, no-op.
 * - Prefer using AppiumDriverLocalService (Appium CLI via node + appium main JS if provided).
 * - If that cannot be started, fall back to launching 'npx appium' as an external process.
 * - After start, perform a /status health check and enforce Appium major version from config.
 */
public final class AppiumManager {

    private static AppiumDriverLocalService service;
    private static Process externalProcess;

    private AppiumManager() {
    }

    public static synchronized void startIfLocal() {
        // Only start Appium programmatically when explicitly enabled.
        // In CI/Docker, Appium is typically provided externally.
        if (!Config.get().startLocalAppium()) {
            System.out.println("AppiumManager: appium.local=false; will not try to start a local Appium server.");
            return;
        }

        // If running in Docker/CI and an external Appium endpoint is provided, never try to start Appium locally.
        // This avoids trying to execute 'npx' inside the Maven test container.
        // External endpoint can be provided via APPIUM_SERVER_URL env var or appiumServerUrl property.
        try {
            String envUrl = System.getenv("APPIUM_SERVER_URL");
            if (envUrl != null && !envUrl.isBlank()) {
                System.out.println("AppiumManager: APPIUM_SERVER_URL is set ('" + envUrl + "'); skipping local Appium start.");
                return;
            }
            String url = Config.get().appiumServerUrl();
            if (url != null) {
                String normalized = url.trim().toLowerCase();
                // If URL is not loopback, treat as external.
                boolean isLoopback = normalized.contains("127.0.0.1") || normalized.contains("localhost");
                if (!isLoopback) {
                    System.out.println("AppiumManager: appiumServerUrl points to an external host ('" + url + "'); skipping local Appium start.");
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        if (service != null && service.isRunning()) {
            return;
        }
        if (externalProcess != null && externalProcess.isAlive()) {
            return;
        }

        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .withIPAddress(Config.get().appiumHost())
                .usingPort(Config.get().appiumPort());

        String nodePath = Config.get().nodePath();
        String appiumJsPath = Config.get().appiumJsPath();
        boolean haveExplicitAppiumJs = nodePath != null && appiumJsPath != null && !nodePath.isBlank() && !appiumJsPath.isBlank();
        if (haveExplicitAppiumJs) {
            builder.usingDriverExecutable(new File(nodePath));
            builder.withAppiumJS(new File(appiumJsPath));
        }

        int requiredMajor = Config.get().appiumMajorVersion();
        int timeoutSec = Config.get().appiumStartTimeoutSeconds();

        int portToUse = Config.get().appiumPort();

        // Prefer AppiumDriverLocalService when we have explicit node/appium.js.
        // If not, we may fall back to npx *only if enabled*.
        if (!haveExplicitAppiumJs) {
            System.out.println("AppiumManager: appium.local=true but appium.node/appium.js not provided.");
        }

        if (haveExplicitAppiumJs) {
            try {
                service = AppiumDriverLocalService.buildService(builder);
                service.start();

                if (service.isRunning()) {
                    System.out.println("Started Appium service via AppiumDriverLocalService on " + Config.get().appiumHost() + ":" + portToUse);

                    if (!validateAppiumVersion(requiredMajor, timeoutSec, portToUse)) {
                        try {
                            service.stop();
                        } catch (Exception ignored) {
                        }
                        service = null;
                        throw new IllegalStateException("Appium server does not meet required major version: " + requiredMajor);
                    }
                    return;
                }
            } catch (Throwable t) {
                System.out.println("Failed to start AppiumDriverLocalService: " + t.getMessage());
                service = null;
            }
        }

        // Fallback to external npx invocation
        if (!Config.get().appiumFallbackEnabled()) {
            throw new IllegalStateException(
                    "Local Appium start is enabled (appium.local=true) but AppiumDriverLocalService could not be started " +
                            "(missing/invalid appium.node/appium.js) and fallback is disabled (appium.fallback.enabled=false). " +
                            "Either provide appium.node/appium.js or disable appium.local and point to an external Appium using appiumServerUrl/APPIUM_SERVER_URL.");
        }

        try {
            // Ensure the port we will use is free or compatible
            try {
                if (!isPortFree(portToUse)) {
                    String currentStatus = fetchStatus(portToUse);
                    Integer runningMajor = null;
                    if (currentStatus != null && !currentStatus.isBlank()) {
                        runningMajor = extractMajorVersion(currentStatus);
                    }

                    if (runningMajor != null && (requiredMajor == 0 || runningMajor == requiredMajor)) {
                        System.out.println("Found existing compatible Appium server on " + Config.get().appiumHost() + ":" + portToUse + " (major=" + runningMajor + "). Reusing it.");
                        return; // reuse existing
                    }

                    int basePort = Config.get().appiumPort();
                    int newPort = findFreePort(basePort + 1, basePort + 50);
                    if (newPort == -1) {
                        throw new IllegalStateException("Configured port " + portToUse + " is in use and no alternative free port found to start Appium " + requiredMajor);
                    }
                    System.out.println("Port " + portToUse + " is in use (runningMajor=" + runningMajor + "); selected free port " + newPort + " for Appium " + requiredMajor);
                    portToUse = newPort;
                }
            } catch (Exception e) {
                System.out.println("Port check failed: " + e.getMessage() + ". Proceeding to attempt start on port " + portToUse);
            }

            List<String> cmd = new ArrayList<>();
            String npx = isWindows() ? "npx.cmd" : "npx";
            cmd.add(npx);

            if (requiredMajor > 0) {
                cmd.add("--yes");
                cmd.add("--package");
                cmd.add("appium@" + requiredMajor);
                cmd.add("appium");
            } else {
                cmd.add("appium");
            }

            cmd.add("--address");
            cmd.add(Config.get().appiumHost());
            cmd.add("--port");
            cmd.add(String.valueOf(portToUse));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);

            File logFile = new File(System.getProperty("user.dir"), "target/appium.log");
            File parent = logFile.getParentFile();
            if (parent != null && !parent.exists()) {
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
            }
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));

            externalProcess = pb.start();

            Thread.sleep(2500);

            if (externalProcess.isAlive()) {
                System.out.println("Started external Appium process via npx on " + Config.get().appiumHost() + ":" + portToUse);
                System.out.println("Appium external process logs: " + logFile.getAbsolutePath());

                if (!validateAppiumVersion(requiredMajor, timeoutSec, portToUse)) {
                    System.out.println("Appium major version mismatch for external process; shutting it down.");
                    externalProcess.destroyForcibly();
                    externalProcess = null;
                    throw new IllegalStateException("Appium server does not meet required major version: " + requiredMajor);
                }
            } else {
                System.out.println("External Appium process terminated immediately. See logs: " + logFile.getAbsolutePath());
                externalProcess = null;
            }
        } catch (IOException | InterruptedException e) {
            // Provide a clearer error if npx is missing (common in minimal CI containers)
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.toLowerCase().contains("cannot run program \"npx\"") || msg.toLowerCase().contains("no such file") || msg.toLowerCase().contains("error=2")) {
                System.out.println("Failed to start external Appium process: 'npx' was not found. Install Node.js/npm in the same environment that runs tests, or disable appium.local and use an external Appium server.");
            } else {
                System.out.println("Failed to start external Appium process: " + msg);
            }
            externalProcess = null;
        }
    }

    public static synchronized AppiumDriverLocalService getService() {
        return service;
    }

    // Stop any Appium services/processes that this manager started. Safe to call multiple times.
    public static synchronized void stopIfStarted() {
        if (service != null) {
            try {
                if (service.isRunning()) {
                    service.stop();
                }
            } catch (Exception ignored) {
            } finally {
                service = null;
            }
        }

        if (externalProcess != null) {
            try {
                externalProcess.destroy();
                // wait a short time for graceful shutdown
                externalProcess.waitFor(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                try {
                    externalProcess.destroyForcibly();
                } catch (Exception ignored2) {
                }
            } finally {
                externalProcess = null;
            }
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    // Poll /status until server responds or timeout. Validate build.version major matches requiredMajor.
    private static boolean validateAppiumVersion(int requiredMajor, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                String status = fetchStatus();
                if (status != null && !status.isBlank()) {
                    Integer major = extractMajorVersion(status);
                    if (major != null) {
                        System.out.println("Detected Appium major version: " + major);
                        boolean match = (requiredMajor == 0) || (major == requiredMajor);
                        if (!match) {
                            System.out.println("Appium major version (" + major + ") does not match required: " + requiredMajor);
                        }
                        return match;
                    }
                }
            } catch (Exception e) {
                // ignore and retry
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        System.out.println("Timed out waiting for Appium /status response.");
        return false;
    }

    private static String fetchStatus() throws IOException {
        String host = Config.get().appiumHost();
        int port = Config.get().appiumPort();
        return fetchStatus(port);
    }

    private static boolean validateAppiumVersion(int requiredMajor, int timeoutSeconds, int port) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                String status = fetchStatus(port);
                if (status != null && !status.isBlank()) {
                    Integer major = extractMajorVersion(status);
                    if (major != null) {
                        System.out.println("Detected Appium major version on port " + port + ": " + major);
                        boolean match = (requiredMajor == 0) || (major == requiredMajor);
                        if (!match) {
                            System.out.println("Appium major version (" + major + ") on port " + port + " does not match required: " + requiredMajor);
                        }
                        return match;
                    }
                }
            } catch (Exception e) {
                // ignore and retry
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        System.out.println("Timed out waiting for Appium /status response on port " + port + ".");
        return false;
    }

    private static String fetchStatus(int port) throws IOException {
        String host = Config.get().appiumHost();
        String url = "http://" + host + ":" + port + "/status";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int rc = conn.getResponseCode();
            if (rc >= 200 && rc < 300) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                return sb.toString();
            }
        } finally {
            conn.disconnect();
        }
        return null;
    }

    private static Integer extractMajorVersion(String json) {
        try {
            JsonElement el = JsonParser.parseString(json);
            if (el != null && el.isJsonObject()) {
                JsonObject obj = el.getAsJsonObject();
                // Try Appium 2.x shape: { "build": { "version": "2.0.0" }, ... }
                if (obj.has("build") && obj.get("build").isJsonObject()) {
                    JsonObject build = obj.getAsJsonObject("build");
                    if (build.has("version") && build.get("version").isJsonPrimitive()) {
                        String version = build.get("version").getAsString();
                        String[] parts = version.split("\\.");
                        if (parts.length > 0) {
                            return Integer.parseInt(parts[0]);
                        }
                    }
                }
                // Try Appium older shape: { "value": { "build": { "version": "1.22.0" } } }
                if (obj.has("value") && obj.get("value").isJsonObject()) {
                    JsonObject value = obj.getAsJsonObject("value");
                    if (value.has("build") && value.get("build").isJsonObject()) {
                        JsonObject build = value.getAsJsonObject("build");
                        if (build.has("version") && build.get("version").isJsonPrimitive()) {
                            String version = build.get("version").getAsString();
                            String[] parts = version.split("\\.");
                            if (parts.length > 0) {
                                return Integer.parseInt(parts[0]);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isPortFree(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static int findFreePort(int startInclusive, int endInclusive) {
        for (int p = startInclusive; p <= endInclusive; p++) {
            if (isPortFree(p)) {
                return p;
            }
        }
        return -1;
    }
}
