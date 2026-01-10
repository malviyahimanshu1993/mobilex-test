package com.framework.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Data utilities for reading test data from JSON, CSV, and properties files.
 */
public final class DataUtils {

    private static final Logger log = LogManager.getLogger(DataUtils.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private DataUtils() {
    }

    // ==================== JSON Operations ====================

    /**
     * Reads JSON file and returns as JsonObject.
     */
    public static JsonObject readJsonFile(String filePath) {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to read JSON file: {}", filePath, e);
            return new JsonObject();
        }
    }

    /**
     * Reads JSON file from classpath.
     */
    public static JsonObject readJsonFromClasspath(String resourceName) {
        try (InputStream is = DataUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                log.error("Resource not found: {}", resourceName);
                return new JsonObject();
            }
            return JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
        } catch (Exception e) {
            log.error("Failed to read JSON from classpath: {}", resourceName, e);
            return new JsonObject();
        }
    }

    /**
     * Reads JSON array from file.
     */
    public static JsonArray readJsonArrayFile(String filePath) {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return JsonParser.parseReader(reader).getAsJsonArray();
        } catch (Exception e) {
            log.error("Failed to read JSON array file: {}", filePath, e);
            return new JsonArray();
        }
    }

    /**
     * Parses JSON to specified class.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Parses JSON to specified type (for generics).
     */
    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Converts object to JSON string.
     */
    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * Writes object to JSON file.
     */
    public static void writeJsonFile(String filePath, Object object) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(filePath))) {
            GSON.toJson(object, writer);
            log.info("Written JSON to: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to write JSON file: {}", filePath, e);
        }
    }

    /**
     * Gets nested value from JSON using dot notation path.
     * Example: getJsonValue(json, "user.address.city")
     */
    public static String getJsonValue(JsonObject json, String path) {
        String[] keys = path.split("\\.");
        JsonObject current = json;

        for (int i = 0; i < keys.length - 1; i++) {
            if (!current.has(keys[i]) || current.get(keys[i]).isJsonNull()) {
                return null;
            }
            current = current.getAsJsonObject(keys[i]);
        }

        String lastKey = keys[keys.length - 1];
        if (current.has(lastKey) && !current.get(lastKey).isJsonNull()) {
            return current.get(lastKey).getAsString();
        }
        return null;
    }

    // ==================== CSV Operations ====================

    /**
     * Reads CSV file and returns as list of maps (headers as keys).
     */
    public static List<Map<String, String>> readCsvFile(String filePath) {
        return readCsvFile(filePath, ",");
    }

    /**
     * Reads CSV file with custom delimiter.
     */
    public static List<Map<String, String>> readCsvFile(String filePath, String delimiter) {
        List<Map<String, String>> data = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            String headerLine = br.readLine();
            if (headerLine == null) return data;

            String[] headers = headerLine.split(delimiter);
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter, -1);
                Map<String, String> row = new LinkedHashMap<>();

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                data.add(row);
            }
        } catch (Exception e) {
            log.error("Failed to read CSV file: {}", filePath, e);
        }

        return data;
    }

    /**
     * Reads CSV file from classpath.
     */
    public static List<Map<String, String>> readCsvFromClasspath(String resourceName) {
        List<Map<String, String>> data = new ArrayList<>();

        try (InputStream is = DataUtils.class.getClassLoader().getResourceAsStream(resourceName);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String headerLine = br.readLine();
            if (headerLine == null) return data;

            String[] headers = headerLine.split(",");
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                Map<String, String> row = new LinkedHashMap<>();

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                data.add(row);
            }
        } catch (Exception e) {
            log.error("Failed to read CSV from classpath: {}", resourceName, e);
        }

        return data;
    }

    /**
     * Converts CSV data to TestNG DataProvider format (Object[][]).
     */
    public static Object[][] csvToDataProvider(String filePath) {
        List<Map<String, String>> data = readCsvFile(filePath);
        if (data.isEmpty()) return new Object[0][0];

        Object[][] result = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = data.get(i);
        }
        return result;
    }

    // ==================== Properties Operations ====================

    /**
     * Reads properties file.
     */
    public static Properties readPropertiesFile(String filePath) {
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
            props.load(is);
        } catch (Exception e) {
            log.error("Failed to read properties file: {}", filePath, e);
        }
        return props;
    }

    /**
     * Reads properties from classpath.
     */
    public static Properties readPropertiesFromClasspath(String resourceName) {
        Properties props = new Properties();
        try (InputStream is = DataUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            log.error("Failed to read properties from classpath: {}", resourceName, e);
        }
        return props;
    }

    // ==================== Test Data Utilities ====================

    /**
     * Generates random string of specified length.
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Generates random email.
     */
    public static String randomEmail() {
        return "test." + randomString(8).toLowerCase() + "@example.com";
    }

    /**
     * Generates random phone number.
     */
    public static String randomPhoneNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("+1");
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Generates random number in range.
     */
    public static int randomNumber(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    /**
     * Gets environment-specific test data.
     */
    public static Map<String, String> getEnvironmentData(String environment) {
        String dataFile = "testdata/" + environment + "/data.json";
        JsonObject json = readJsonFromClasspath(dataFile);

        Map<String, String> data = new HashMap<>();
        for (String key : json.keySet()) {
            if (!json.get(key).isJsonNull() && json.get(key).isJsonPrimitive()) {
                data.put(key, json.get(key).getAsString());
            }
        }
        return data;
    }

    /**
     * Creates a unique test identifier.
     */
    public static String uniqueTestId() {
        return "TEST_" + System.currentTimeMillis() + "_" + randomString(4);
    }

    /**
     * Reads test data for specific test case.
     */
    public static Map<String, String> getTestCaseData(String testCaseName) {
        JsonObject allData = readJsonFromClasspath("testdata/testcases.json");
        if (allData.has(testCaseName)) {
            JsonObject testData = allData.getAsJsonObject(testCaseName);
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            return GSON.fromJson(testData, type);
        }
        return new HashMap<>();
    }
}

