package com.framework.listeners;

import com.framework.annotations.DataDriven;
import com.framework.utils.DataUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dynamic data provider that reads test data from various sources.
 */
public class DynamicDataProvider {

    private static final Logger log = LogManager.getLogger(DynamicDataProvider.class);

    /**
     * Provides data from CSV file.
     */
    @DataProvider(name = "csvData")
    public static Object[][] csvDataProvider(Method method) {
        DataDriven annotation = method.getAnnotation(DataDriven.class);
        if (annotation == null) {
            return new Object[0][0];
        }

        String dataFile = annotation.dataFile();
        List<Map<String, String>> data;

        if (annotation.classpath()) {
            data = DataUtils.readCsvFromClasspath(dataFile);
        } else {
            data = DataUtils.readCsvFile(dataFile);
        }

        // Apply filter if specified
        String filter = annotation.filter();
        if (!filter.isBlank()) {
            data = applyFilter(data, filter);
        }

        log.info("Loaded {} test data rows from {}", data.size(), dataFile);

        Object[][] result = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = data.get(i);
        }
        return result;
    }

    /**
     * Provides data from JSON file.
     */
    @DataProvider(name = "jsonData")
    public static Object[][] jsonDataProvider(Method method) {
        DataDriven annotation = method.getAnnotation(DataDriven.class);
        if (annotation == null) {
            return new Object[0][0];
        }

        String dataFile = annotation.dataFile();
        JsonArray jsonArray;

        if (annotation.classpath()) {
            jsonArray = DataUtils.readJsonFromClasspath(dataFile).getAsJsonArray("testData");
        } else {
            jsonArray = DataUtils.readJsonArrayFile(dataFile);
        }

        if (jsonArray == null) {
            return new Object[0][0];
        }

        log.info("Loaded {} test data entries from {}", jsonArray.size(), dataFile);

        Object[][] result = new Object[jsonArray.size()][1];
        for (int i = 0; i < jsonArray.size(); i++) {
            result[i][0] = jsonArray.get(i).getAsJsonObject();
        }
        return result;
    }

    /**
     * Provides test data from multiple sources dynamically.
     */
    @DataProvider(name = "dynamicData")
    public static Object[][] dynamicDataProvider(Method method) {
        DataDriven annotation = method.getAnnotation(DataDriven.class);
        if (annotation == null) {
            return new Object[0][0];
        }

        String dataFile = annotation.dataFile();

        if (dataFile.endsWith(".csv")) {
            return csvDataProvider(method);
        } else if (dataFile.endsWith(".json")) {
            return jsonDataProvider(method);
        } else {
            log.warn("Unsupported data file format: {}", dataFile);
            return new Object[0][0];
        }
    }

    /**
     * Provides inline test data.
     */
    @DataProvider(name = "inlineData")
    public static Object[][] inlineDataProvider() {
        return new Object[][]{
                {"user1", "pass1"},
                {"user2", "pass2"},
                {"user3", "pass3"}
        };
    }

    /**
     * Parallel data provider.
     */
    @DataProvider(name = "parallelData", parallel = true)
    public static Object[][] parallelDataProvider(Method method) {
        return dynamicDataProvider(method);
    }

    private static List<Map<String, String>> applyFilter(List<Map<String, String>> data, String filter) {
        List<Map<String, String>> filtered = new ArrayList<>();

        try {
            // Simple filter format: "key=value" or "key!=value"
            String[] parts;
            boolean notEquals = filter.contains("!=");

            if (notEquals) {
                parts = filter.split("!=", 2);
            } else {
                parts = filter.split("=", 2);
            }

            if (parts.length != 2) {
                log.warn("Invalid filter format: {}", filter);
                return data;
            }

            String key = parts[0].trim();
            String value = parts[1].trim();

            for (Map<String, String> row : data) {
                String actual = row.get(key);
                boolean matches = value.equals(actual);

                if (notEquals ? !matches : matches) {
                    filtered.add(row);
                }
            }

            log.debug("Filter '{}' reduced data from {} to {} rows", filter, data.size(), filtered.size());
        } catch (Exception e) {
            log.error("Failed to apply filter: {}", filter, e);
            return data;
        }

        return filtered;
    }
}

