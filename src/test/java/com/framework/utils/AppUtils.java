package com.framework.utils;

import com.framework.base.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.HidesKeyboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

/**
 * Utilities for app-level operations.
 */
public final class AppUtils {

    private static final Logger log = LogManager.getLogger(AppUtils.class);

    private AppUtils() {
    }

    public static void closeApp() {
        if (!DriverManager.isAndroid()) return;

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            String bundleId = getCurrentAppPackage();
            if (bundleId != null && !bundleId.isEmpty()) {
                driver.terminateApp(bundleId);
                log.info("App terminated: {}", bundleId);
            }
        } catch (Exception e) {
            log.error("Failed to close app: {}", e.getMessage());
        }
    }

    public static void launchApp(String appPackage) {
        if (!DriverManager.isAndroid()) return;

        try {
            DriverManager.getAndroidDriver().activateApp(appPackage);
            log.info("App launched: {}", appPackage);
        } catch (Exception e) {
            log.error("Failed to launch app: {}", e.getMessage());
        }
    }

    public static void backgroundApp(Duration duration) {
        if (!DriverManager.isAndroid()) return;

        try {
            DriverManager.getAndroidDriver().runAppInBackground(duration);
            log.info("App sent to background for {} seconds", duration.getSeconds());
        } catch (Exception e) {
            log.error("Failed to background app: {}", e.getMessage());
        }
    }

    public static boolean isAppInstalled(String bundleId) {
        if (!DriverManager.isAndroid()) return false;

        try {
            return DriverManager.getAndroidDriver().isAppInstalled(bundleId);
        } catch (Exception e) {
            log.error("Failed to check app installation: {}", e.getMessage());
            return false;
        }
    }

    public static String getCurrentAppPackage() {
        if (!DriverManager.isAndroid()) {
            return "";
        }

        try {
            return DriverManager.getAndroidDriver().getCurrentPackage();
        } catch (Exception e) {
            log.error("Failed to get current package: {}", e.getMessage());
            return "";
        }
    }

    public static void pressBack() {
        if (!DriverManager.isAndroid()) {
            return;
        }

        try {
            DriverManager.getAndroidDriver().pressKey(new KeyEvent(AndroidKey.BACK));
            log.debug("Pressed back button");
        } catch (Exception e) {
            log.error("Failed to press back button: {}", e.getMessage());
        }
    }

    public static void pressHome() {
        if (!DriverManager.isAndroid()) {
            return;
        }

        try {
            DriverManager.getAndroidDriver().pressKey(new KeyEvent(AndroidKey.HOME));
            log.debug("Pressed home button");
        } catch (Exception e) {
            log.error("Failed to press home button: {}", e.getMessage());
        }
    }

    public static void hideKeyboard() {
        if (!DriverManager.isAndroid()) return;

        try {
            DriverManager.getAndroidDriver().hideKeyboard();
            log.debug("Keyboard hidden");
        } catch (Exception e) {
            log.debug("Failed to hide keyboard: {}", e.getMessage());
        }
    }

    public static boolean isKeyboardShown() {
        if (!DriverManager.isAndroid()) {
            return false;
        }

        try {
            return DriverManager.getAndroidDriver().isKeyboardShown();
        } catch (Exception e) {
            return false;
        }
    }

    public static String getOrientation() {
        if (!DriverManager.isAndroid()) return "UNKNOWN";

        try {
            return DriverManager.getAndroidDriver().getOrientation().name();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}

