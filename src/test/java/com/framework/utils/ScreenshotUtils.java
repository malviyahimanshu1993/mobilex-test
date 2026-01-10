package com.framework.utils;

import com.framework.base.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Screenshot utilities for capturing, saving, and attaching screenshots.
 */
public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {
    }

    /**
     * Takes a screenshot and returns it as bytes.
     */
    public static byte[] takeScreenshotAsBytes() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            log.warn("Driver is null, cannot take screenshot");
            return new byte[0];
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.error("Failed to take screenshot: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Takes a screenshot and returns it as Base64 string.
     */
    public static String takeScreenshotAsBase64() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return "";
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            log.error("Failed to take screenshot: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Takes a screenshot and saves it to a file.
     */
    public static File takeScreenshotAsFile(String fileName) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return null;
        }
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destPath = Paths.get(SCREENSHOT_DIR, fileName);
            Files.createDirectories(destPath.getParent());
            Files.copy(srcFile.toPath(), destPath);
            log.info("Screenshot saved to: {}", destPath.toAbsolutePath());
            return destPath.toFile();
        } catch (Exception e) {
            log.error("Failed to save screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Takes a timestamped screenshot.
     */
    public static File takeTimestampedScreenshot(String prefix) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String fileName = prefix + "_" + timestamp + ".png";
        return takeScreenshotAsFile(fileName);
    }

    /**
     * Attaches screenshot to Allure report.
     */
    public static void attachToAllure(String name) {
        byte[] screenshot = takeScreenshotAsBytes();
        if (screenshot.length > 0) {
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
            log.info("Screenshot attached to Allure: {}", name);
        }
    }

    /**
     * Takes screenshot on failure and attaches to Allure.
     */
    public static void captureOnFailure(String testName) {
        attachToAllure("failure-" + testName);
        takeTimestampedScreenshot("failure_" + testName);
    }

    /**
     * Takes an element screenshot.
     */
    public static byte[] takeElementScreenshot(WebElement element) {
        try {
            return element.getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.error("Failed to take element screenshot: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Takes element screenshot and attaches to Allure.
     */
    public static void attachElementToAllure(WebElement element, String name) {
        byte[] screenshot = takeElementScreenshot(element);
        if (screenshot.length > 0) {
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
        }
    }

    /**
     * Captures full page source and saves it.
     */
    public static void capturePageSource(String fileName) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return;
        }
        try {
            String pageSource = driver.getPageSource();
            Path filePath = Paths.get(SCREENSHOT_DIR, fileName + ".xml");
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, pageSource);
            log.info("Page source saved to: {}", filePath.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to save page source: {}", e.getMessage());
        }
    }

    /**
     * Attaches page source to Allure report.
     */
    public static void attachPageSourceToAllure(String name) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return;
        }
        try {
            String pageSource = driver.getPageSource();
            Allure.addAttachment(name, "application/xml", pageSource, ".xml");
        } catch (Exception e) {
            log.error("Failed to attach page source: {}", e.getMessage());
        }
    }

    /**
     * Compares two screenshots for visual differences.
     * Returns similarity percentage (100 = identical).
     */
    public static double compareScreenshots(byte[] screenshot1, byte[] screenshot2) {
        try {
            BufferedImage img1 = ImageIO.read(new ByteArrayInputStream(screenshot1));
            BufferedImage img2 = ImageIO.read(new ByteArrayInputStream(screenshot2));

            if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                return 0.0;
            }

            long diff = 0;
            for (int y = 0; y < img1.getHeight(); y++) {
                for (int x = 0; x < img1.getWidth(); x++) {
                    int rgb1 = img1.getRGB(x, y);
                    int rgb2 = img2.getRGB(x, y);

                    int r1 = (rgb1 >> 16) & 0xff;
                    int g1 = (rgb1 >> 8) & 0xff;
                    int b1 = rgb1 & 0xff;

                    int r2 = (rgb2 >> 16) & 0xff;
                    int g2 = (rgb2 >> 8) & 0xff;
                    int b2 = rgb2 & 0xff;

                    diff += Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                }
            }

            double maxDiff = 3L * 255 * img1.getWidth() * img1.getHeight();
            double similarity = 100.0 * (1.0 - diff / maxDiff);
            return Math.round(similarity * 100.0) / 100.0;
        } catch (IOException e) {
            log.error("Failed to compare screenshots: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Takes a full-page screenshot with stitching (for scrollable content).
     */
    public static byte[] takeFullPageScreenshot() {
        // For now, delegate to regular screenshot
        // Full implementation would require scrolling and stitching
        return takeScreenshotAsBytes();
    }

    /**
     * Gets the screenshot directory path.
     */
    public static Path getScreenshotDirectory() {
        Path path = Paths.get(SCREENSHOT_DIR);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            log.error("Failed to create screenshot directory: {}", e.getMessage());
        }
        return path;
    }

    /**
     * Cleans up old screenshots (older than specified days).
     */
    public static void cleanupOldScreenshots(int daysOld) {
        try {
            Path screenshotPath = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(screenshotPath)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L);
            Files.list(screenshotPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted old screenshot: {}", path);
                        } catch (IOException e) {
                            log.warn("Failed to delete screenshot: {}", path);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to cleanup screenshots: {}", e.getMessage());
        }
    }
}

