package com.framework.reporting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom HTML report generator for test execution summary.
 */
public final class HtmlReportGenerator {

    private static final Logger log = LogManager.getLogger(HtmlReportGenerator.class);
    private static final String REPORT_DIR = "target/reports";

    private static final Map<String, TestResult> testResults = new ConcurrentHashMap<>();
    private static LocalDateTime suiteStartTime;
    private static LocalDateTime suiteEndTime;
    private static String suiteName = "Test Suite";

    private HtmlReportGenerator() {
    }

    public static void startSuite(String name) {
        suiteName = name;
        suiteStartTime = LocalDateTime.now();
        testResults.clear();
    }

    public static void endSuite() {
        suiteEndTime = LocalDateTime.now();
    }

    public static void recordTestResult(String testName, String className, Status status,
                                         long durationMs, String errorMessage) {
        TestResult result = new TestResult();
        result.testName = testName;
        result.className = className;
        result.status = status;
        result.durationMs = durationMs;
        result.errorMessage = errorMessage;
        result.timestamp = LocalDateTime.now();

        testResults.put(className + "." + testName, result);
    }

    public static void generateReport() {
        if (suiteEndTime == null) {
            suiteEndTime = LocalDateTime.now();
        }

        try {
            Path reportPath = Paths.get(REPORT_DIR);
            Files.createDirectories(reportPath);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path htmlFile = reportPath.resolve("test-report-" + timestamp + ".html");

            try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile.toFile()))) {
                writeHtmlReport(writer);
            }

            log.info("HTML report generated: {}", htmlFile.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to generate HTML report: {}", e.getMessage());
        }
    }

    private static void writeHtmlReport(PrintWriter writer) {
        int passed = 0, failed = 0, skipped = 0;
        long totalDuration = 0;

        for (TestResult result : testResults.values()) {
            switch (result.status) {
                case PASSED: passed++; break;
                case FAILED: failed++; break;
                case SKIPPED: skipped++; break;
            }
            totalDuration += result.durationMs;
        }

        int total = passed + failed + skipped;
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;

        writer.println("<!DOCTYPE html>");
        writer.println("<html lang=\"en\">");
        writer.println("<head>");
        writer.println("  <meta charset=\"UTF-8\">");
        writer.println("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        writer.println("  <title>Test Execution Report</title>");
        writer.println("  <style>");
        writer.println(getStyles());
        writer.println("  </style>");
        writer.println("</head>");
        writer.println("<body>");

        // Header
        writer.println("<div class=\"header\">");
        writer.println("  <h1>🔬 Test Execution Report</h1>");
        writer.println("  <p>" + suiteName + " | " +
                suiteStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
        writer.println("</div>");

        // Summary Cards
        writer.println("<div class=\"summary\">");
        writer.printf("  <div class=\"card total\"><h3>%d</h3><p>Total Tests</p></div>%n", total);
        writer.printf("  <div class=\"card passed\"><h3>%d</h3><p>Passed</p></div>%n", passed);
        writer.printf("  <div class=\"card failed\"><h3>%d</h3><p>Failed</p></div>%n", failed);
        writer.printf("  <div class=\"card skipped\"><h3>%d</h3><p>Skipped</p></div>%n", skipped);
        writer.printf("  <div class=\"card rate\"><h3>%.1f%%</h3><p>Pass Rate</p></div>%n", passRate);
        writer.printf("  <div class=\"card duration\"><h3>%s</h3><p>Duration</p></div>%n",
                formatDuration(totalDuration));
        writer.println("</div>");

        // Progress Bar
        writer.println("<div class=\"progress-container\">");
        writer.printf("  <div class=\"progress-bar passed\" style=\"width: %.1f%%\"></div>%n",
                total > 0 ? (passed * 100.0 / total) : 0);
        writer.printf("  <div class=\"progress-bar failed\" style=\"width: %.1f%%\"></div>%n",
                total > 0 ? (failed * 100.0 / total) : 0);
        writer.printf("  <div class=\"progress-bar skipped\" style=\"width: %.1f%%\"></div>%n",
                total > 0 ? (skipped * 100.0 / total) : 0);
        writer.println("</div>");

        // Test Results Table
        writer.println("<h2>Test Results</h2>");
        writer.println("<table>");
        writer.println("  <thead>");
        writer.println("    <tr><th>Status</th><th>Test Name</th><th>Class</th><th>Duration</th><th>Error</th></tr>");
        writer.println("  </thead>");
        writer.println("  <tbody>");

        List<TestResult> sortedResults = new ArrayList<>(testResults.values());
        sortedResults.sort((a, b) -> {
            if (a.status != b.status) {
                return a.status.ordinal() - b.status.ordinal();
            }
            return a.testName.compareTo(b.testName);
        });

        for (TestResult result : sortedResults) {
            String statusClass = result.status.name().toLowerCase();
            String statusIcon = result.status == Status.PASSED ? "✓" :
                               result.status == Status.FAILED ? "✗" : "⊘";
            String error = result.errorMessage != null ?
                    result.errorMessage.substring(0, Math.min(100, result.errorMessage.length())) : "";

            writer.printf("    <tr class=\"%s\">%n", statusClass);
            writer.printf("      <td><span class=\"status %s\">%s %s</span></td>%n",
                    statusClass, statusIcon, result.status);
            writer.printf("      <td>%s</td>%n", result.testName);
            writer.printf("      <td>%s</td>%n", result.className);
            writer.printf("      <td>%s</td>%n", formatDuration(result.durationMs));
            writer.printf("      <td class=\"error\">%s</td>%n", error);
            writer.println("    </tr>");
        }

        writer.println("  </tbody>");
        writer.println("</table>");

        // Footer
        writer.println("<div class=\"footer\">");
        writer.println("  <p>Generated by MobileX Test Framework</p>");
        writer.println("</div>");

        writer.println("</body>");
        writer.println("</html>");
    }

    private static String getStyles() {
        return """
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: 'Segoe UI', Tahoma, sans-serif; background: #f5f5f5; color: #333; }
                .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; 
                          padding: 30px; text-align: center; }
                .header h1 { font-size: 2em; margin-bottom: 10px; }
                .summary { display: flex; flex-wrap: wrap; justify-content: center; gap: 15px; 
                           padding: 20px; margin: -30px 20px 20px; }
                .card { background: white; border-radius: 10px; padding: 20px; min-width: 120px;
                        text-align: center; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                .card h3 { font-size: 2em; margin-bottom: 5px; }
                .card.passed h3 { color: #27ae60; }
                .card.failed h3 { color: #e74c3c; }
                .card.skipped h3 { color: #f39c12; }
                .card.rate h3 { color: #3498db; }
                .progress-container { display: flex; height: 8px; background: #ddd; margin: 0 20px 30px;
                                       border-radius: 4px; overflow: hidden; }
                .progress-bar { height: 100%; }
                .progress-bar.passed { background: #27ae60; }
                .progress-bar.failed { background: #e74c3c; }
                .progress-bar.skipped { background: #f39c12; }
                h2 { padding: 0 20px; margin-bottom: 15px; }
                table { width: calc(100% - 40px); margin: 0 20px; border-collapse: collapse;
                        background: white; border-radius: 10px; overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #eee; }
                th { background: #667eea; color: white; font-weight: 600; }
                tr:hover { background: #f8f9fa; }
                .status { padding: 4px 12px; border-radius: 20px; font-size: 0.85em; font-weight: 600; }
                .status.passed { background: #d4edda; color: #155724; }
                .status.failed { background: #f8d7da; color: #721c24; }
                .status.skipped { background: #fff3cd; color: #856404; }
                .error { color: #721c24; font-size: 0.85em; max-width: 300px; 
                         overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
                .footer { text-align: center; padding: 30px; color: #666; }
                """;
    }

    private static String formatDuration(long ms) {
        if (ms < 1000) return ms + "ms";
        if (ms < 60000) return String.format("%.1fs", ms / 1000.0);
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        return String.format("%dm %ds", minutes, seconds);
    }

    public enum Status {
        PASSED, FAILED, SKIPPED
    }

    private static class TestResult {
        String testName;
        String className;
        Status status;
        long durationMs;
        String errorMessage;
        LocalDateTime timestamp;
    }
}

