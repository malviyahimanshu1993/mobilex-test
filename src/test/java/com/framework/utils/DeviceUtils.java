package com.framework.utils;

import com.framework.base.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Device information and management utilities.
 */
public final class DeviceUtils {

    private static final Logger log = LogManager.getLogger(DeviceUtils.class);

    private DeviceUtils() {
    }

    /**
     * Gets device information as a map.
     */
    public static Map<String, String> getDeviceInfo() {
        Map<String, String> info = new HashMap<>();

        try {
            if (DriverManager.isAndroid()) {
                AndroidDriver driver = DriverManager.getAndroidDriver();

                info.put("platform", "Android");
                info.put("platformVersion", getAndroidVersion());
                info.put("deviceName", (String) driver.getCapabilities().getCapability("deviceName"));
                info.put("udid", (String) driver.getCapabilities().getCapability("udid"));
                info.put("automationName", (String) driver.getCapabilities().getCapability("automationName"));

                Dimension size = driver.manage().window().getSize();
                info.put("screenWidth", String.valueOf(size.getWidth()));
                info.put("screenHeight", String.valueOf(size.getHeight()));

                info.put("batteryLevel", String.valueOf(getBatteryLevel()));
                info.put("isLocked", String.valueOf(driver.isDeviceLocked()));
            }
        } catch (Exception e) {
            log.error("Failed to get device info: {}", e.getMessage());
        }

        return info;
    }

    /**
     * Gets Android OS version.
     */
    public static String getAndroidVersion() {
        if (!DriverManager.isAndroid()) {
            return "";
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "getprop");
            args.put("args", new String[]{"ro.build.version.release"});
            Object result = driver.executeScript("mobile: shell", args);
            return result != null ? result.toString().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets device manufacturer.
     */
    public static String getManufacturer() {
        if (!DriverManager.isAndroid()) {
            return "";
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "getprop");
            args.put("args", new String[]{"ro.product.manufacturer"});
            Object result = driver.executeScript("mobile: shell", args);
            return result != null ? result.toString().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets device model.
     */
    public static String getModel() {
        if (!DriverManager.isAndroid()) {
            return "";
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "getprop");
            args.put("args", new String[]{"ro.product.model"});
            Object result = driver.executeScript("mobile: shell", args);
            return result != null ? result.toString().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets battery level (0-100).
     */
    public static int getBatteryLevel() {
        if (!DriverManager.isAndroid()) {
            return -1;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "dumpsys");
            args.put("args", new String[]{"battery"});
            Object result = driver.executeScript("mobile: shell", args);
            if (result != null) {
                String output = result.toString();
                for (String line : output.split("\n")) {
                    if (line.trim().startsWith("level:")) {
                        return Integer.parseInt(line.split(":")[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get battery level: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * Gets available storage in MB.
     */
    public static long getAvailableStorageMB() {
        if (!DriverManager.isAndroid()) {
            return -1;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "df");
            args.put("args", new String[]{"/data"});
            Object result = driver.executeScript("mobile: shell", args);
            if (result != null) {
                String[] lines = result.toString().split("\n");
                if (lines.length > 1) {
                    String[] parts = lines[1].trim().split("\\s+");
                    if (parts.length >= 4) {
                        String available = parts[3].replace("G", "").replace("M", "").replace("K", "");
                        double value = Double.parseDouble(available);
                        if (parts[3].contains("G")) value *= 1024;
                        if (parts[3].contains("K")) value /= 1024;
                        return (long) value;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get storage info: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * Gets device time.
     */
    public static String getDeviceTime() {
        if (!DriverManager.isAndroid()) {
            return "";
        }

        try {
            return DriverManager.getAndroidDriver().getDeviceTime();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Checks if device is an emulator.
     */
    public static boolean isEmulator() {
        if (!DriverManager.isAndroid()) {
            return false;
        }

        try {
            String model = getModel().toLowerCase();
            String manufacturer = getManufacturer().toLowerCase();
            return model.contains("sdk") || model.contains("emulator") ||
                   manufacturer.contains("genymotion") || manufacturer.contains("google");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets screen density.
     */
    public static int getScreenDensity() {
        if (!DriverManager.isAndroid()) {
            return -1;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "wm");
            args.put("args", new String[]{"density"});
            Object result = driver.executeScript("mobile: shell", args);
            if (result != null) {
                String output = result.toString();
                String[] parts = output.split(":");
                if (parts.length > 1) {
                    return Integer.parseInt(parts[1].trim());
                }
            }
        } catch (Exception e) {
            log.error("Failed to get screen density: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * Gets screen resolution.
     */
    public static Dimension getScreenResolution() {
        try {
            return DriverManager.getDriver().manage().window().getSize();
        } catch (Exception e) {
            return new Dimension(0, 0);
        }
    }

    /**
     * Takes a bugreport (Android only) - for debugging purposes.
     */
    public static void captureBugreport(String fileName) {
        if (!DriverManager.isAndroid()) {
            return;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "bugreportz");
            args.put("args", new String[]{});
            driver.executeScript("mobile: shell", args);
            log.info("Bugreport captured: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to capture bugreport: {}", e.getMessage());
        }
    }

    /**
     * Gets current CPU usage percentage.
     */
    public static String getCpuUsage() {
        if (!DriverManager.isAndroid()) {
            return "";
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "top");
            args.put("args", new String[]{"-n", "1", "-b"});
            Object result = driver.executeScript("mobile: shell", args);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets memory info.
     */
    public static Map<String, Long> getMemoryInfo() {
        Map<String, Long> memInfo = new HashMap<>();

        if (!DriverManager.isAndroid()) {
            return memInfo;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            Map<String, Object> args = new HashMap<>();
            args.put("command", "cat");
            args.put("args", new String[]{"/proc/meminfo"});
            Object result = driver.executeScript("mobile: shell", args);

            if (result != null) {
                for (String line : result.toString().split("\n")) {
                    if (line.startsWith("MemTotal:")) {
                        memInfo.put("total", parseMemValue(line));
                    } else if (line.startsWith("MemFree:")) {
                        memInfo.put("free", parseMemValue(line));
                    } else if (line.startsWith("MemAvailable:")) {
                        memInfo.put("available", parseMemValue(line));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get memory info: {}", e.getMessage());
        }

        return memInfo;
    }

    private static long parseMemValue(String line) {
        try {
            String[] parts = line.split(":");
            String value = parts[1].trim().replace("kB", "").trim();
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Sets device time (requires root or special permissions).
     */
    public static void setDeviceTime(LocalDateTime dateTime) {
        if (!DriverManager.isAndroid()) {
            return;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();
            String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("MMddHHmmyyyy.ss"));

            Map<String, Object> args = new HashMap<>();
            args.put("command", "date");
            args.put("args", new String[]{formattedDate});
            driver.executeScript("mobile: shell", args);
            log.info("Device time set to: {}", dateTime);
        } catch (Exception e) {
            log.error("Failed to set device time: {}", e.getMessage());
        }
    }

    /**
     * Restarts ADB server.
     */
    public static void restartAdb() {
        if (!DriverManager.isAndroid()) {
            return;
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("adb kill-server").waitFor();
            Thread.sleep(1000);
            runtime.exec("adb start-server").waitFor();
            log.info("ADB server restarted");
        } catch (Exception e) {
            log.error("Failed to restart ADB: {}", e.getMessage());
        }
    }
}

