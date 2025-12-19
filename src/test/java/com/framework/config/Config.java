package com.framework.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {

    private static final Config INSTANCE = new Config();
    private final Properties fileProps = new Properties();

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
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return expandVars(sys);
        String fileVal = fileProps.getProperty(key);
        if (fileVal != null && !fileVal.isBlank()) return expandVars(fileVal);
        return expandVars(defaultValue);
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
                // no closing brace, append rest and break
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
        return get("appiumServerUrl", "http://127.0.0.1:4723");
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

    /** Optional: set if Appium is not on PATH and you want programmatic service start. */
    public String nodePath() {
        return get("appium.node", "");
    }

    /** Optional: full path to Appium main JS (Appium 2). */
    public String appiumJsPath() {
        return get("appium.js", "");
    }

    public String appPath() {
        String defaultApp = "bundle-to-test/android/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk";
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

    // Enforce required Appium major version (default 2 -> only Appium 2.x allowed)
    public int appiumMajorVersion() {
        return Integer.parseInt(get("appium.major.version", "2"));
    }

    // Timeout (seconds) to wait for Appium /status during startup validation
    public int appiumStartTimeoutSeconds() {
        return Integer.parseInt(get("appium.start.timeout.seconds", "30"));
    }

    // Whether fallback to 'npx appium' is allowed if AppiumDriverLocalService start fails
    public boolean appiumFallbackEnabled() {
        return Boolean.parseBoolean(get("appium.fallback.enabled", "true"));
    }
}
