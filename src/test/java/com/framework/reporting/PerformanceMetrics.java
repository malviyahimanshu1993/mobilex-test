package com.framework.reporting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance metrics collector for test execution analysis.
 */
public final class PerformanceMetrics {

    private static final Logger log = LogManager.getLogger(PerformanceMetrics.class);

    // Metrics storage
    private static final Map<String, List<Long>> pageLoadTimes = new ConcurrentHashMap<>();
    private static final Map<String, List<Long>> actionTimes = new ConcurrentHashMap<>();
    private static final Map<String, Instant> activeTimers = new ConcurrentHashMap<>();
    private static final List<MemorySnapshot> memorySnapshots = Collections.synchronizedList(new ArrayList<>());

    private PerformanceMetrics() {
    }

    // ==================== Timer Operations ====================

    /**
     * Starts a named timer.
     */
    public static void startTimer(String name) {
        activeTimers.put(name, Instant.now());
        log.debug("Timer started: {}", name);
    }

    /**
     * Stops a timer and returns elapsed milliseconds.
     */
    public static long stopTimer(String name) {
        Instant start = activeTimers.remove(name);
        if (start == null) {
            log.warn("Timer '{}' was not started", name);
            return 0;
        }
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        log.debug("Timer stopped: {} - {}ms", name, elapsed);
        return elapsed;
    }

    /**
     * Stops timer and records as page load time.
     */
    public static long stopTimerAsPageLoad(String pageName) {
        long elapsed = stopTimer(pageName);
        recordPageLoadTime(pageName, elapsed);
        return elapsed;
    }

    /**
     * Stops timer and records as action time.
     */
    public static long stopTimerAsAction(String actionName) {
        long elapsed = stopTimer(actionName);
        recordActionTime(actionName, elapsed);
        return elapsed;
    }

    // ==================== Metric Recording ====================

    /**
     * Records page load time.
     */
    public static void recordPageLoadTime(String pageName, long timeMs) {
        pageLoadTimes.computeIfAbsent(pageName, k -> Collections.synchronizedList(new ArrayList<>()))
                     .add(timeMs);
        log.info("Page '{}' loaded in {}ms", pageName, timeMs);
    }

    /**
     * Records action execution time.
     */
    public static void recordActionTime(String actionName, long timeMs) {
        actionTimes.computeIfAbsent(actionName, k -> Collections.synchronizedList(new ArrayList<>()))
                   .add(timeMs);
        log.debug("Action '{}' completed in {}ms", actionName, timeMs);
    }

    /**
     * Takes a memory snapshot.
     */
    public static void takeMemorySnapshot(String label) {
        Runtime runtime = Runtime.getRuntime();
        MemorySnapshot snapshot = new MemorySnapshot();
        snapshot.label = label;
        snapshot.timestamp = Instant.now();
        snapshot.totalMemory = runtime.totalMemory();
        snapshot.freeMemory = runtime.freeMemory();
        snapshot.usedMemory = snapshot.totalMemory - snapshot.freeMemory;
        snapshot.maxMemory = runtime.maxMemory();

        memorySnapshots.add(snapshot);
        log.debug("Memory snapshot '{}': used={}MB, free={}MB",
                label, snapshot.usedMemory / (1024 * 1024), snapshot.freeMemory / (1024 * 1024));
    }

    // ==================== Statistics ====================

    /**
     * Gets page load statistics.
     */
    public static Map<String, Statistics> getPageLoadStatistics() {
        Map<String, Statistics> stats = new LinkedHashMap<>();
        for (Map.Entry<String, List<Long>> entry : pageLoadTimes.entrySet()) {
            stats.put(entry.getKey(), calculateStatistics(entry.getValue()));
        }
        return stats;
    }

    /**
     * Gets action time statistics.
     */
    public static Map<String, Statistics> getActionStatistics() {
        Map<String, Statistics> stats = new LinkedHashMap<>();
        for (Map.Entry<String, List<Long>> entry : actionTimes.entrySet()) {
            stats.put(entry.getKey(), calculateStatistics(entry.getValue()));
        }
        return stats;
    }

    /**
     * Gets memory snapshots.
     */
    public static List<MemorySnapshot> getMemorySnapshots() {
        return new ArrayList<>(memorySnapshots);
    }

    /**
     * Generates performance summary report.
     */
    public static String generateSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== PERFORMANCE SUMMARY ==========\n\n");

        // Page Load Times
        sb.append("--- Page Load Times ---\n");
        for (Map.Entry<String, Statistics> entry : getPageLoadStatistics().entrySet()) {
            Statistics stat = entry.getValue();
            sb.append(String.format("  %s: avg=%.0fms, min=%dms, max=%dms, p95=%.0fms (n=%d)\n",
                    entry.getKey(), stat.average, stat.min, stat.max, stat.percentile95, stat.count));
        }

        sb.append("\n--- Action Times ---\n");
        for (Map.Entry<String, Statistics> entry : getActionStatistics().entrySet()) {
            Statistics stat = entry.getValue();
            sb.append(String.format("  %s: avg=%.0fms, min=%dms, max=%dms (n=%d)\n",
                    entry.getKey(), stat.average, stat.min, stat.max, stat.count));
        }

        sb.append("\n--- Memory Usage ---\n");
        for (MemorySnapshot snapshot : memorySnapshots) {
            sb.append(String.format("  %s: used=%dMB, free=%dMB, total=%dMB\n",
                    snapshot.label,
                    snapshot.usedMemory / (1024 * 1024),
                    snapshot.freeMemory / (1024 * 1024),
                    snapshot.totalMemory / (1024 * 1024)));
        }

        sb.append("\n==========================================\n");
        return sb.toString();
    }

    /**
     * Clears all collected metrics.
     */
    public static void clearAll() {
        pageLoadTimes.clear();
        actionTimes.clear();
        activeTimers.clear();
        memorySnapshots.clear();
    }

    private static Statistics calculateStatistics(List<Long> values) {
        Statistics stats = new Statistics();
        if (values == null || values.isEmpty()) {
            return stats;
        }

        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);

        stats.count = values.size();
        stats.min = sorted.get(0);
        stats.max = sorted.get(sorted.size() - 1);
        stats.sum = values.stream().mapToLong(Long::longValue).sum();
        stats.average = stats.sum / (double) stats.count;

        // Standard deviation
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - stats.average, 2))
                .average()
                .orElse(0);
        stats.stdDev = Math.sqrt(variance);

        // Percentiles
        stats.median = getPercentile(sorted, 50);
        stats.percentile90 = getPercentile(sorted, 90);
        stats.percentile95 = getPercentile(sorted, 95);
        stats.percentile99 = getPercentile(sorted, 99);

        return stats;
    }

    private static double getPercentile(List<Long> sorted, int percentile) {
        if (sorted.isEmpty()) return 0;
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }

    // ==================== Data Classes ====================

    public static class Statistics {
        public int count;
        public long min;
        public long max;
        public long sum;
        public double average;
        public double stdDev;
        public double median;
        public double percentile90;
        public double percentile95;
        public double percentile99;

        @Override
        public String toString() {
            return String.format("avg=%.0f, min=%d, max=%d, p95=%.0f (n=%d)",
                    average, min, max, percentile95, count);
        }
    }

    public static class MemorySnapshot {
        public String label;
        public Instant timestamp;
        public long totalMemory;
        public long freeMemory;
        public long usedMemory;
        public long maxMemory;
    }
}

