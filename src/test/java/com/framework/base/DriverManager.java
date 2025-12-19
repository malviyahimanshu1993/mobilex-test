package com.framework.base;

import com.framework.config.Config;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.AutomationName;

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
}
