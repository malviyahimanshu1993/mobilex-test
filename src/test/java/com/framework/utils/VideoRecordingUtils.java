package com.framework.utils;

import com.framework.base.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.android.AndroidStopScreenRecordingOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Video recording utilities for test execution.
 */
public final class VideoRecordingUtils {

    private static final Logger log = LogManager.getLogger(VideoRecordingUtils.class);
    private static final String VIDEO_DIR = "target/videos";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static boolean isRecording = false;
    private static String currentRecordingName;

    private VideoRecordingUtils() {
    }

    /**
     * Starts screen recording on Android device.
     */
    public static void startRecording() {
        startRecording("recording_" + LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * Starts screen recording with custom name.
     */
    public static void startRecording(String name) {
        if (!DriverManager.isAndroid()) {
            log.warn("Screen recording is only supported on Android");
            return;
        }

        if (isRecording) {
            log.warn("Recording already in progress");
            return;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();

            AndroidStartScreenRecordingOptions options = new AndroidStartScreenRecordingOptions()
                    .withTimeLimit(Duration.ofMinutes(5))
                    .withVideoSize("1280x720")
                    .withBitRate(3000000);

            driver.startRecordingScreen(options);
            isRecording = true;
            currentRecordingName = name;

            log.info("Screen recording started: {}", name);

        } catch (Exception e) {
            log.error("Failed to start screen recording: {}", e.getMessage());
        }
    }

    /**
     * Stops screen recording and saves video file.
     * @return Path to saved video file, or null if failed
     */
    public static Path stopRecording() {
        if (!DriverManager.isAndroid()) {
            return null;
        }

        if (!isRecording) {
            log.warn("No recording in progress");
            return null;
        }

        try {
            AndroidDriver driver = DriverManager.getAndroidDriver();

            String base64Video = driver.stopRecordingScreen(
                    new AndroidStopScreenRecordingOptions());

            isRecording = false;

            // Decode and save video
            byte[] videoBytes = Base64.getDecoder().decode(base64Video);
            Path videoPath = saveVideo(videoBytes, currentRecordingName);

            log.info("Screen recording stopped and saved: {}", videoPath);
            return videoPath;

        } catch (Exception e) {
            log.error("Failed to stop screen recording: {}", e.getMessage());
            isRecording = false;
            return null;
        }
    }

    /**
     * Stops recording and saves with custom name.
     */
    public static Path stopRecording(String name) {
        currentRecordingName = name;
        return stopRecording();
    }

    /**
     * Checks if recording is in progress.
     */
    public static boolean isRecording() {
        return isRecording;
    }

    /**
     * Stops recording if in progress (cleanup method).
     */
    public static void stopIfRecording() {
        if (isRecording) {
            stopRecording();
        }
    }

    /**
     * Starts recording for a test.
     */
    public static void startTestRecording(String testName) {
        String sanitizedName = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
        startRecording("test_" + sanitizedName + "_" + LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }

    /**
     * Stops recording for a test.
     */
    public static Path stopTestRecording(String testName, boolean passed) {
        String status = passed ? "passed" : "failed";
        String sanitizedName = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
        currentRecordingName = "test_" + sanitizedName + "_" + status;
        return stopRecording();
    }

    private static Path saveVideo(byte[] videoBytes, String name) {
        try {
            Path videoDir = Paths.get(VIDEO_DIR);
            Files.createDirectories(videoDir);

            String fileName = name + ".mp4";
            Path videoPath = videoDir.resolve(fileName);

            try (FileOutputStream fos = new FileOutputStream(videoPath.toFile())) {
                fos.write(videoBytes);
            }

            return videoPath;

        } catch (Exception e) {
            log.error("Failed to save video: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets the video directory path.
     */
    public static Path getVideoDirectory() {
        Path path = Paths.get(VIDEO_DIR);
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            log.error("Failed to create video directory: {}", e.getMessage());
        }
        return path;
    }

    /**
     * Cleans up old video files.
     */
    public static void cleanupOldVideos(int daysOld) {
        try {
            Path videoPath = Paths.get(VIDEO_DIR);
            if (!Files.exists(videoPath)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L);

            Files.list(videoPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".mp4"))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted old video: {}", path);
                        } catch (Exception e) {
                            log.warn("Failed to delete video: {}", path);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to cleanup videos: {}", e.getMessage());
        }
    }
}

