package com.framework.base;

import com.framework.config.Config;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.AutomationName;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class DriverManager {

    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    // Initializes driver for the current thread. platform: "Android" or "iOS"
    public static void initDriver(String platform) throws MalformedURLException {
        if (DRIVER.get() != null) return;

        String serverUrl = resolveServerUrl();

        // Fail fast if Appium is not reachable from this JVM/container.
        assertAppiumReachable(serverUrl);

        if ("iOS".equalsIgnoreCase(platform)) {
            DRIVER.set(createIOSDriver(serverUrl));
        } else {
            DRIVER.set(createAndroidDriver(serverUrl));
        }
    }

    public static AppiumDriver getDriver() {
        return DRIVER.get();
    }

    public static AndroidDriver getAndroidDriver() {
        AppiumDriver d = getDriver();
        return d instanceof AndroidDriver ? (AndroidDriver) d : null;
    }

    public static IOSDriver getIOSDriver() {
        AppiumDriver d = getDriver();
        return d instanceof IOSDriver ? (IOSDriver) d : null;
    }

    public static boolean isAndroid() {
        return getDriver() instanceof AndroidDriver;
    }

    public static boolean isIOS() {
        return getDriver() instanceof IOSDriver;
    }

    public static void quitDriver() {
        AppiumDriver d = DRIVER.get();
        if (d == null) return;
        try {
            d.quit();
        } catch (Exception ignored) {
        } finally {
            DRIVER.remove();
        }
    }

    private static AndroidDriver createAndroidDriver(String serverUrl) throws MalformedURLException {
        Config cfg = Config.get();

        UiAutomator2Options options = new UiAutomator2Options()
                .setAutomationName(AutomationName.ANDROID_UIAUTOMATOR2)
                .setPlatformName("Android")
                .setDeviceName(cfg.deviceName())
                .setUdid(cfg.udid())
                .setNewCommandTimeout(Duration.ofSeconds(cfg.newCommandTimeoutSeconds()))
                .autoGrantPermissions();

        // Resolve APK path: allow relative paths in config (resolved against project working dir)
        String appPathStr = cfg.appPath();
        System.out.println("Original appPath from config: '" + appPathStr + "'");

        // If Jenkins provided the Windows host workspace, prefer that path
        String hostWorkspace = System.getenv("HOST_WORKSPACE");
        if (hostWorkspace != null && !hostWorkspace.isBlank()) {
            System.out.println("Detected HOST_WORKSPACE env var: " + hostWorkspace);
            // Try to compute a relative path starting from the repository root (bundle-to-test/...)
            String relPath = appPathStr;
            int idx = relPath.indexOf("bundle-to-test");
            if (idx != -1) {
                relPath = relPath.substring(idx);
            } else {
                // strip common container prefixes like /workspace/
                if (relPath.startsWith("/workspace/")) relPath = relPath.substring("/workspace/".length());
                else if (relPath.startsWith("/")) relPath = relPath.substring(1);
            }
            // normalize separators for the host filesystem
            relPath = relPath.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);
            Path apk = Path.of(hostWorkspace).resolve(relPath);
            System.out.println("Resolved APK path using HOST_WORKSPACE: " + apk.toAbsolutePath());
            if (!Files.exists(apk)) {
                // In Docker the container cannot verify Windows host paths. Do NOT fail here.
                System.out.println("WARNING: APK not found from inside the container at '" + apk.toAbsolutePath() + "'.\n" +
                        "This is expected when using HOST_WORKSPACE: the container cannot see the Windows filesystem C:\\ path.\n" +
                        "Proceeding to send the Windows path to Appium on the host; Appium must resolve the file.");
            }
            options.setApp(apk.toAbsolutePath().toString());
            System.out.println("Expanded appPath: '" + appPathStr + "'");
        } else {
            // Fallback expansion for placeholders like ${user.dir} if present
            if (appPathStr != null && appPathStr.contains("${")) {
                // Quick expansions for common placeholders
                String userDir = System.getProperty("user.dir", "");
                appPathStr = appPathStr.replace("${user.dir}", userDir)
                                       .replace("$user.dir", userDir)
                                       .replace("%USERPROFILE%", System.getProperty("user.home", ""))
                                       .replace("%userprofile%", System.getProperty("user.home", ""));

                appPathStr = appPathStr.replace("${user.dir}", System.getProperty("user.dir"));
                // support common environment variable style without braces if any
                appPathStr = appPathStr.replace("$user.dir", System.getProperty("user.dir"));
            }
            // Normalize separators
            appPathStr = appPathStr.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);

            Path apk = Path.of(appPathStr);
            if (!apk.isAbsolute()) {
                apk = Path.of(System.getProperty("user.dir")).resolve(apk);
            }
            System.out.println("Resolved APK path: " + apk.toAbsolutePath());
            if (!Files.exists(apk)) {
                // Fallback: try replacing any ${user.dir} placeholders explicitly and stripping unknown placeholders
                String cleaned = appPathStr.replace("${user.dir}", System.getProperty("user.dir"));
                cleaned = cleaned.replaceAll("\\$\\{[^}]+\\}", "");
                Path alt = Path.of(cleaned);
                if (!alt.isAbsolute()) alt = Path.of(System.getProperty("user.dir")).resolve(alt);
                System.out.println("Initial APK not found. Trying alternative resolved path: " + alt.toAbsolutePath());
                if (Files.exists(alt)) {
                    apk = alt;
                } else {
                    throw new IllegalStateException("APK not found at '" + apk.toAbsolutePath() + "'. Set -DappPath if required.");
                }
            }
            options.setApp(apk.toAbsolutePath().toString());
            System.out.println("Expanded appPath: '" + appPathStr + "'");
        }


        if (!cfg.appPackage().isBlank()) {
            options.setAppPackage(cfg.appPackage());
        }
        if (!cfg.appActivity().isBlank()) {
            options.setAppActivity(cfg.appActivity());
        }

        // Helpful defaults
        options.setCapability("adbExecTimeout", 600000);
        options.setCapability("appWaitActivity", cfg.appActivity());
        options.setCapability("appWaitPackage", cfg.appPackage());
        options.setCapability("appWaitDuration", 30000);

        return new AndroidDriver(new URL(serverUrl), options);
    }

    private static IOSDriver createIOSDriver(String serverUrl) throws MalformedURLException {
        Config cfg = Config.get();

        XCUITestOptions options = new XCUITestOptions()
                .setAutomationName(AutomationName.IOS_XCUI_TEST)
                .setPlatformName("iOS")
                .setDeviceName(cfg.deviceName())
                .setBundleId(cfg.bundleId())
                .setNewCommandTimeout(Duration.ofSeconds(cfg.newCommandTimeoutSeconds()));

        return new IOSDriver(new URL(serverUrl), options);
    }

    private static String resolveServerUrl() {
        // Keep existing behavior: accept either base or /wd/hub.
        String url = Config.get().appiumServerUrl().trim();
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return url;
    }

    private static void assertAppiumReachable(String serverUrl) {
        // Accept both base and /wd/hub style URLs.
        String statusUrl = serverUrl.endsWith("/wd/hub") ? serverUrl + "/status" : serverUrl + "/status";
        long deadline = System.currentTimeMillis() + 30_000L;
        Exception last = null;

        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(statusUrl).openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();

                if (code >= 200 && code < 500) {
                    // 2xx is good; 4xx still proves the server is reachable (path mismatch handled separately).
                    return;
                }
            } catch (Exception e) {
                last = e;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        String hint = "";
        if (serverUrl.contains("host.docker.internal")) {
            hint = " If Appium runs on the Windows host, ensure it is started with --address 0.0.0.0 (not 127.0.0.1) and that Windows Firewall allows TCP 4723.";
        }
        throw new IllegalStateException("Cannot reach Appium server at '" + statusUrl + "'." + hint + (last != null ? (" Last error: " + last) : ""));
    }
}
