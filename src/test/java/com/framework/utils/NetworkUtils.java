package com.framework.utils;

import com.framework.base.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Network and connectivity utilities for mobile testing.
 */
public final class NetworkUtils {

    private static final Logger log = LogManager.getLogger(NetworkUtils.class);

    private NetworkUtils() {
    }

    /**
     * Toggles airplane mode (Android only).
     */
    public static void toggleAirplaneMode(boolean enable) {
        if (!DriverManager.isAndroid()) {
            log.warn("Airplane mode toggle is only supported on Android");
            return;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "settings");
            args.put("args", new String[]{"put", "global", "airplane_mode_on", enable ? "1" : "0"});
            driver.executeScript("mobile: shell", args);

            // Broadcast the change
            Map<String, Object> broadcastArgs = new HashMap<>();
            broadcastArgs.put("command", "am");
            broadcastArgs.put("args", new String[]{"broadcast", "-a", "android.intent.action.AIRPLANE_MODE"});
            driver.executeScript("mobile: shell", broadcastArgs);

            log.info("Airplane mode set to: {}", enable);
        } catch (Exception e) {
            log.error("Failed to toggle airplane mode: {}", e.getMessage());
        }
    }

    /**
     * Toggles WiFi (Android only).
     */
    public static void toggleWifi(boolean enable) {
        if (!DriverManager.isAndroid()) {
            log.warn("WiFi toggle is only supported on Android");
            return;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "svc");
            args.put("args", new String[]{"wifi", enable ? "enable" : "disable"});
            driver.executeScript("mobile: shell", args);
            log.info("WiFi set to: {}", enable);
        } catch (Exception e) {
            log.error("Failed to toggle WiFi: {}", e.getMessage());
        }
    }

    /**
     * Toggles mobile data (Android only).
     */
    public static void toggleMobileData(boolean enable) {
        if (!DriverManager.isAndroid()) {
            log.warn("Mobile data toggle is only supported on Android");
            return;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "svc");
            args.put("args", new String[]{"data", enable ? "enable" : "disable"});
            driver.executeScript("mobile: shell", args);
            log.info("Mobile data set to: {}", enable);
        } catch (Exception e) {
            log.error("Failed to toggle mobile data: {}", e.getMessage());
        }
    }

    /**
     * Simulates network conditions (if supported by device/emulator).
     */
    public static void setNetworkSpeed(NetworkSpeed speed) {
        if (!DriverManager.isAndroid()) {
            return;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            // This requires emulator with specific settings
            Map<String, Object> args = new HashMap<>();
            args.put("networkSpeed", speed.name().toLowerCase());
            driver.executeScript("mobile: setNetworkSpeed", args);
            log.info("Network speed set to: {}", speed);
        } catch (Exception e) {
            log.warn("Network speed simulation not supported: {}", e.getMessage());
        }
    }

    /**
     * Gets the current network connection type.
     */
    public static String getConnectionType() {
        if (!DriverManager.isAndroid()) {
            return "UNKNOWN";
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            return driver.getConnection().toString();
        } catch (Exception e) {
            log.error("Failed to get connection type: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Checks if device has network connectivity.
     */
    public static boolean hasNetworkConnectivity() {
        if (!DriverManager.isAndroid()) {
            return true; // Assume yes for iOS
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "ping");
            args.put("args", new String[]{"-c", "1", "-W", "2", "8.8.8.8"});
            Object result = driver.executeScript("mobile: shell", args);
            return result != null && result.toString().contains("1 received");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for network connectivity.
     */
    public static boolean waitForConnectivity(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            if (hasNetworkConnectivity()) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Enables all network connections.
     */
    public static void enableAllConnections() {
        toggleAirplaneMode(false);
        WaitUtils.hardWait(500);
        toggleWifi(true);
        toggleMobileData(true);
        log.info("All network connections enabled");
    }

    /**
     * Disables all network connections.
     */
    public static void disableAllConnections() {
        toggleAirplaneMode(true);
        log.info("All network connections disabled");
    }

    /**
     * Network speed simulation options.
     */
    public enum NetworkSpeed {
        FULL,
        GSM,       // ~14.4 kbps
        HSCSD,     // ~43.2 kbps
        GPRS,      // ~80 kbps
        EDGE,      // ~240 kbps
        UMTS,      // ~384 kbps
        HSDPA,     // ~3.6 Mbps
        LTE,       // ~100 Mbps
        EVDO,      // ~750 kbps
        NONE
    }

    /**
     * Gets device IP address.
     */
    public static String getDeviceIpAddress() {
        if (!DriverManager.isAndroid()) {
            return "";
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "ip");
            args.put("args", new String[]{"addr", "show", "wlan0"});
            Object result = driver.executeScript("mobile: shell", args);
            if (result != null) {
                String output = result.toString();
                // Parse IP from output like "inet 192.168.1.100/24"
                int inetIndex = output.indexOf("inet ");
                if (inetIndex >= 0) {
                    String ipPart = output.substring(inetIndex + 5);
                    int slashIndex = ipPart.indexOf('/');
                    if (slashIndex > 0) {
                        return ipPart.substring(0, slashIndex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get device IP: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Opens URL in device browser.
     */
    public static void openUrlInBrowser(String url) {
        if (!DriverManager.isAndroid()) {
            return;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "am");
            args.put("args", new String[]{"start", "-a", "android.intent.action.VIEW", "-d", url});
            driver.executeScript("mobile: shell", args);
            log.info("Opened URL in browser: {}", url);
        } catch (Exception e) {
            log.error("Failed to open URL: {}", e.getMessage());
        }
    }
}

