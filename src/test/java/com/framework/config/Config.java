package com.framework.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuration management for the test framework.
 *
 * Supports easy switching between local and Docker execution:
 * - Local:  mvn test -Denv=local   (uses http://127.0.0.1:4723)
 * - Docker: mvn test -Denv=docker  (uses http://host.docker.internal:4723)
 *
 * Or use Maven profiles:
 * - mvn test -Plocal
 * - mvn test -Pdocker
 */
public final class Config {

    private static final Config INSTANCE = new Config();
    private final Properties fileProps = new Properties();

    // Environment constants
    public static final String ENV_LOCAL = "local";
    public static final String ENV_DOCKER = "docker";

    // Appium URLs for different environments
    private static final String APPIUM_URL_LOCAL = "http://127.0.0.1:4723";
    private static final String APPIUM_URL_DOCKER = "http://host.docker.internal:4723";

    private Config() {
        // Load properties from file if provided via -Dconfig.file or from classpath resource 'config.properties'
        String configFile = System.getProperty("config.file", "");
        if (!configFile.isBlank()) {
            Path p = Path.of(configFile);
            if (Files.exists(p)) {
                try (InputStream is = new FileInputStream(p.toFile())) {
                    fileProps.load(is);
                } catch (Exception ignored) {
                }
            }
        } else {
            // try to load from classpath
            try (InputStream is = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (is != null) {
                    fileProps.load(is);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static Config get() {
        return INSTANCE;
    }

    private String get(String key, String defaultValue) {
        // Priority: ENV var > System property > config.properties > default

        // Special handling for appiumServerUrl based on env flag
        if ("appiumServerUrl".equals(key)) {
            // First check explicit APPIUM_SERVER_URL env var
            String envUrl = System.getenv("APPIUM_SERVER_URL");
            if (envUrl != null && !envUrl.isBlank()) return expandVars(envUrl);

            // Then check explicit system property
            String sysUrl = System.getProperty("appiumServerUrl");
            if (sysUrl != null && !sysUrl.isBlank()) return expandVars(sysUrl);

            // Otherwise, derive from env flag
            return getAppiumUrlForEnvironment();
        }

        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return expandVars(sys);
        String fileVal = fileProps.getProperty(key);
        if (fileVal != null && !fileVal.isBlank()) return expandVars(fileVal);
        return expandVars(defaultValue);
    }

    /**
     * Gets the current execution environment (local or docker).
     * Override with -Denv=local or -Denv=docker
     */
    public String env() {
        // Check system property first
        String envProp = System.getProperty("env");
        if (envProp != null && !envProp.isBlank()) {
            return envProp.toLowerCase();
        }

        // Check environment variable
        String envVar = System.getenv("ENV");
        if (envVar != null && !envVar.isBlank()) {
            return envVar.toLowerCase();
        }

        // Check config file
        String fileVal = fileProps.getProperty("env");
        if (fileVal != null && !fileVal.isBlank()) {
            return fileVal.toLowerCase();
        }

        // Default to local
        return ENV_LOCAL;
    }

    /**
     * Checks if running in local mode.
     */
    public boolean isLocal() {
        return ENV_LOCAL.equals(env());
    }

    /**
     * Checks if running in Docker/CI mode.
     */
    public boolean isDocker() {
        return ENV_DOCKER.equals(env());
    }

    /**
     * Gets the appropriate Appium URL based on execution environment.
     */
    private String getAppiumUrlForEnvironment() {
        if (isDocker()) {
            return APPIUM_URL_DOCKER;
        }
        return APPIUM_URL_LOCAL;
    }

    // Replace ${name} placeholders using Java system properties first, then environment variables.
    private String expandVars(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        while (true) {
            int start = input.indexOf("${", idx);
            if (start == -1) {
                sb.append(input.substring(idx));
                break;
            }
            sb.append(input.substring(idx, start));
            int end = input.indexOf('}', start + 2);
            if (end == -1) {
                sb.append(input.substring(start));
                break;
            }
            String name = input.substring(start + 2, end);
            String val = System.getProperty(name);
            if (val == null) val = System.getenv(name);
            if (val == null) val = "";
            sb.append(val);
            idx = end + 1;
        }
        return sb.toString();
    }

    public String platform() {
        return get("platform", "Android");
    }

    public String appiumServerUrl() {
        String url = get("appiumServerUrl", APPIUM_URL_LOCAL);
        if (url == null) return null;
        url = url.trim();
        // Normalize: Appium 2 default base-path is '/', not '/wd/hub'.
        if (url.endsWith("/") && !url.endsWith("/wd/hub/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /** Start/stop local Appium 2.x server in code (default: false). */
    public boolean startLocalAppium() {
        return Boolean.parseBoolean(get("appium.local", "false"));
    }

    public String appiumHost() {
        return get("appium.host", "127.0.0.1");
    }

    public int appiumPort() {
        return Integer.parseInt(get("appium.port", "4723"));
    }

    public String nodePath() {
        return get("appium.node", "");
    }

    public String appiumJsPath() {
        return get("appium.js", "");
    }

    public String appPath() {
        String defaultApp = "bundle-to-test/android/swaglab/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk";
        return get("appPath", defaultApp);
    }

    public String udid() {
        return get("udid", "ef7a8b61");
    }

    public String deviceName() {
        return get("deviceName", "RMX1901");
    }

    public String appPackage() {
        return get("appPackage", "com.swaglabsmobileapp");
    }

    public String appActivity() {
        return get("appActivity", "com.swaglabsmobileapp.MainActivity");
    }

    public String bundleId() {
        return get("bundleId", "com.example.ios.app");
    }

    public String username() {
        return get("test.user", "standard_user");
    }

    public String password() {
        return get("test.pass", "secret_sauce");
    }

    public int retryCount() {
        return Integer.parseInt(get("retry.count", "1"));
    }

    public int explicitWaitSeconds() {
        return Integer.parseInt(get("wait.seconds", "10"));
    }

    public int newCommandTimeoutSeconds() {
        return Integer.parseInt(get("newCommandTimeout.seconds", "300"));
    }

    public int parallelThreadCount() {
        return Integer.parseInt(get("thread.count", "1"));
    }

    public int appiumMajorVersion() {
        return Integer.parseInt(get("appium.major.version", "2"));
    }

    public int appiumStartTimeoutSeconds() {
        return Integer.parseInt(get("appium.start.timeout.seconds", "30"));
    }

    public boolean appiumFallbackEnabled() {
        return Boolean.parseBoolean(get("appium.fallback.enabled", "true"));
    }

    public String executionMode() {
        return get("execution.mode", "auto");
    }

    public boolean isLocalMode() {
        return "local".equalsIgnoreCase(executionMode()) || isLocal();
    }

    public boolean isDockerMode() {
        return "docker".equalsIgnoreCase(executionMode()) || isDocker();
    }

    /**
     * Prints current configuration summary (useful for debugging).
     */
    public void printConfiguration() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              MOBILEX TEST CONFIGURATION                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Environment    : %-42s ║%n", env().toUpperCase());
        System.out.printf("║  Appium Server  : %-42s ║%n", appiumServerUrl());
        System.out.printf("║  Platform       : %-42s ║%n", platform());
        System.out.printf("║  Device         : %-42s ║%n", deviceName() + " (" + udid() + ")");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
