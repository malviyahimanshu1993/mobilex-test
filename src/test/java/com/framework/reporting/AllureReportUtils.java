package com.framework.reporting;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Allure reporting utilities.
 */
public final class AllureReportUtils {

    private static final Logger log = LogManager.getLogger(AllureReportUtils.class);
    private static final String ALLURE_RESULTS_DIR = "target/allure-results";

    private AllureReportUtils() {
    }

    public static void generateEnvironmentProperties() {
        Map<String, String> envProps = new LinkedHashMap<>();
        envProps.put("Framework", "MobileX Test Framework");
        envProps.put("Java.Version", System.getProperty("java.version"));
        envProps.put("Execution.Date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        envProps.put("OS.Name", System.getProperty("os.name"));
        writePropertiesFile(envProps, "environment.properties");
    }

    public static void generateCategories() {
        String categories = "[{\"name\": \"Test defects\", \"matchedStatuses\": [\"failed\"]}]";
        writeFile(categories, "categories.json");
    }

    public static void generateExecutorInfo() {
        String buildNumber = System.getenv("BUILD_NUMBER");
        if (buildNumber == null) buildNumber = "local";
        String executor = "{\"name\": \"MobileX Tests\", \"buildName\": \"" + buildNumber + "\"}";
        writeFile(executor, "executor.json");
    }

    public static void attachText(String name, String content) {
        Allure.addAttachment(name, "text/plain", content, ".txt");
    }

    public static void attachJson(String name, String jsonContent) {
        Allure.addAttachment(name, "application/json", jsonContent, ".json");
    }

    public static void attachXml(String name, String xmlContent) {
        Allure.addAttachment(name, "application/xml", xmlContent, ".xml");
    }

    public static void setDescription(String description) {
        Allure.description(description);
    }

    public static void step(String stepName) {
        Allure.step(stepName);
    }

    public static void addIssue(String issueId) {
        Allure.issue(issueId, issueId);
    }

    public static void addTmsLink(String testCaseId) {
        Allure.tms(testCaseId, testCaseId);
    }

    private static void writePropertiesFile(Map<String, String> properties, String fileName) {
        try {
            Path path = Paths.get(ALLURE_RESULTS_DIR);
            Files.createDirectories(path);
            Properties props = new Properties();
            props.putAll(properties);
            try (FileWriter writer = new FileWriter(path.resolve(fileName).toFile())) {
                props.store(writer, "Allure Environment");
            }
        } catch (IOException e) {
            log.error("Failed to write {}: {}", fileName, e.getMessage());
        }
    }

    private static void writeFile(String content, String fileName) {
        try {
            Path path = Paths.get(ALLURE_RESULTS_DIR);
            Files.createDirectories(path);
            Files.writeString(path.resolve(fileName), content);
        } catch (IOException e) {
            log.error("Failed to write {}: {}", fileName, e.getMessage());
        }
    }
}

