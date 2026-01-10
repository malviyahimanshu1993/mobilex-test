package com.framework.listeners;

import com.framework.annotations.TestInfo;
import com.framework.annotations.TestCategory;
import com.framework.utils.ScreenshotUtils;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced test listener with annotation processing and reporting.
 */
public class EnhancedTestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(EnhancedTestListener.class);
    private static final Map<String, Instant> testStartTimes = new ConcurrentHashMap<>();

    @Override
    public void onStart(ITestContext context) {
        log.info("========================================");
        log.info("Test Suite Started: {}", context.getName());
        log.info("========================================");
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("========================================");
        log.info("Test Suite Finished: {}", context.getName());
        log.info("Passed: {}, Failed: {}, Skipped: {}",
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
        log.info("========================================");
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);
        testStartTimes.put(testName, Instant.now());
        log.info(">>> Starting Test: {}", testName);
        processAnnotations(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = getTestName(result);
        long duration = getDuration(testName);
        log.info("Test PASSED: {} ({}ms)", testName, duration);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = getTestName(result);
        long duration = getDuration(testName);
        log.error("Test FAILED: {} ({}ms)", testName, duration);
        ScreenshotUtils.attachToAllure("failure-" + testName);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("Test SKIPPED: {}", getTestName(result));
    }

    private void processAnnotations(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();

        TestInfo info = method.getAnnotation(TestInfo.class);
        if (info == null) {
            info = method.getDeclaringClass().getAnnotation(TestInfo.class);
        }
        if (info != null) {
            if (!info.author().isBlank()) Allure.label("author", info.author());
            if (!info.priority().isBlank()) Allure.label("priority", info.priority());
            if (!info.component().isBlank()) Allure.label("component", info.component());
        }

        TestCategory category = method.getAnnotation(TestCategory.class);
        if (category == null) {
            category = method.getDeclaringClass().getAnnotation(TestCategory.class);
        }
        if (category != null) {
            Allure.label("testType", category.type().name());
            if (!category.feature().isBlank()) Allure.feature(category.feature());
        }
    }

    private String getTestName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getName();
    }

    private long getDuration(String testName) {
        Instant start = testStartTimes.remove(testName);
        return start != null ? Duration.between(start, Instant.now()).toMillis() : 0;
    }
}

