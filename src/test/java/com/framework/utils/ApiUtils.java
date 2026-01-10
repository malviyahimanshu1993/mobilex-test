package com.framework.utils;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple API testing utilities for mobile test scenarios.
 * Useful for setting up test data, validating backend state, etc.
 */
public final class ApiUtils {

    private static final Logger log = LogManager.getLogger(ApiUtils.class);
    private static final int DEFAULT_TIMEOUT = 30;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
            .build();

    private ApiUtils() {
    }

    // ==================== GET Requests ====================

    /**
     * Performs GET request and returns response body.
     */
    public static String get(String url) {
        return get(url, new HashMap<>());
    }

    /**
     * Performs GET request with headers.
     */
    public static String get(String url, Map<String, String> headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT));

            headers.forEach(requestBuilder::header);

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            log.debug("GET {} - Status: {}", url, response.statusCode());
            return response.body();

        } catch (Exception e) {
            log.error("GET request failed: {} - {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * Performs GET request and returns ApiResponse object.
     */
    public static ApiResponse getWithResponse(String url, Map<String, String> headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT));

            headers.forEach(requestBuilder::header);

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.statusCode = response.statusCode();
            apiResponse.body = response.body();
            apiResponse.headers = new HashMap<>();
            response.headers().map().forEach((k, v) -> apiResponse.headers.put(k, String.join(",", v)));

            return apiResponse;

        } catch (Exception e) {
            log.error("GET request failed: {} - {}", url, e.getMessage());
            return null;
        }
    }

    // ==================== POST Requests ====================

    /**
     * Performs POST request with JSON body.
     */
    public static String postJson(String url, String jsonBody) {
        return postJson(url, jsonBody, new HashMap<>());
    }

    /**
     * Performs POST request with JSON body and headers.
     */
    public static String postJson(String url, String jsonBody, Map<String, String> headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT));

            headers.forEach(requestBuilder::header);

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            log.debug("POST {} - Status: {}", url, response.statusCode());
            return response.body();

        } catch (Exception e) {
            log.error("POST request failed: {} - {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * Performs POST request and returns ApiResponse.
     */
    public static ApiResponse postWithResponse(String url, String body, Map<String, String> headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT));

            headers.forEach(requestBuilder::header);

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.statusCode = response.statusCode();
            apiResponse.body = response.body();
            apiResponse.headers = new HashMap<>();
            response.headers().map().forEach((k, v) -> apiResponse.headers.put(k, String.join(",", v)));

            return apiResponse;

        } catch (Exception e) {
            log.error("POST request failed: {} - {}", url, e.getMessage());
            return null;
        }
    }

    // ==================== PUT Requests ====================

    /**
     * Performs PUT request with JSON body.
     */
    public static String putJson(String url, String jsonBody, Map<String, String> headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT));

            headers.forEach(requestBuilder::header);

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            log.debug("PUT {} - Status: {}", url, response.statusCode());
            return response.body();

        } catch (Exception e) {
            log.error("PUT request failed: {} - {}", url, e.getMessage());
            return null;
        }
    }

    // ==================== DELETE Requests ====================

    /**
     * Performs DELETE request.
     */
    public static ApiResponse delete(String url, Map<String, String> headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT));

            headers.forEach(requestBuilder::header);

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.statusCode = response.statusCode();
            apiResponse.body = response.body();

            log.debug("DELETE {} - Status: {}", url, response.statusCode());
            return apiResponse;

        } catch (Exception e) {
            log.error("DELETE request failed: {} - {}", url, e.getMessage());
            return null;
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Checks if URL is reachable.
     */
    public static boolean isReachable(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() < 400;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Downloads file from URL.
     */
    public static boolean downloadFile(String url, String destinationPath) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
                    fos.write(response.body());
                }
                log.info("Downloaded file to: {}", destinationPath);
                return true;
            }

        } catch (Exception e) {
            log.error("Failed to download file: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Attaches API response to Allure report.
     */
    public static void attachToReport(String name, ApiResponse response) {
        if (response == null) return;

        String content = String.format("""
                Status Code: %d
                
                Headers:
                %s
                
                Body:
                %s
                """,
                response.statusCode,
                response.headers.toString(),
                response.body);

        Allure.addAttachment(name, "text/plain", content, ".txt");
    }

    /**
     * Builds URL with query parameters.
     */
    public static String buildUrl(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?");

        params.forEach((key, value) -> {
            url.append(key).append("=").append(value).append("&");
        });

        return url.substring(0, url.length() - 1);
    }

    /**
     * API Response wrapper class.
     */
    public static class ApiResponse {
        public int statusCode;
        public String body;
        public Map<String, String> headers = new HashMap<>();

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        public boolean isClientError() {
            return statusCode >= 400 && statusCode < 500;
        }

        public boolean isServerError() {
            return statusCode >= 500;
        }

        @Override
        public String toString() {
            return String.format("ApiResponse{statusCode=%d, bodyLength=%d}",
                    statusCode, body != null ? body.length() : 0);
        }
    }
}

