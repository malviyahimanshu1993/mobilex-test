package com.framework.listeners;

import com.framework.annotations.FlakyTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced retry analyzer that supports @FlakyTest annotation and configurable retry logic.
 */
public class EnhancedRetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(EnhancedRetryAnalyzer.class);
    private static final ConcurrentHashMap<String, AtomicInteger> retryCounters = new ConcurrentHashMap<>();

    private static final int DEFAULT_MAX_RETRIES = Integer.parseInt(
            System.getProperty("retry.count", "1"));

    @Override
    public boolean retry(ITestResult result) {
        String testId = getTestId(result);
        AtomicInteger counter = retryCounters.computeIfAbsent(testId, k -> new AtomicInteger(0));

        int maxRetries = getMaxRetries(result);
        int currentRetry = counter.get();

        if (currentRetry < maxRetries) {
            int newCount = counter.incrementAndGet();
            log.info("Retrying test '{}' - Attempt {}/{}",
                    result.getName(), newCount, maxRetries);

            // Apply delay if configured
            int delayMs = getRetryDelay(result);
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            return true;
        }

        // Reset counter for next test run
        retryCounters.remove(testId);
        return false;
    }

    private int getMaxRetries(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();

        // Check for @FlakyTest annotation
        FlakyTest flakyTest = method.getAnnotation(FlakyTest.class);
        if (flakyTest != null) {
            log.debug("Test '{}' marked as flaky: {} - Max retries: {}",
                    result.getName(), flakyTest.reason(), flakyTest.maxRetries());
            return flakyTest.maxRetries();
        }

        // Use default
        return DEFAULT_MAX_RETRIES;
    }

    private int getRetryDelay(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();

        FlakyTest flakyTest = method.getAnnotation(FlakyTest.class);
        if (flakyTest != null) {
            return flakyTest.delayMs();
        }

        return Integer.parseInt(System.getProperty("retry.delay.ms", "1000"));
    }

    private String getTestId(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getName() +
               "_" + Thread.currentThread().getId();
    }

    /**
     * Clears all retry counters. Call this at suite start if needed.
     */
    public static void resetAllCounters() {
        retryCounters.clear();
    }

    /**
     * Gets the current retry count for a test.
     */
    public static int getRetryCount(ITestResult result) {
        String testId = result.getTestClass().getName() + "." + result.getName() +
                "_" + Thread.currentThread().getId();
        AtomicInteger counter = retryCounters.get(testId);
        return counter != null ? counter.get() : 0;
    }
}

