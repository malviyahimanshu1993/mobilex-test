package com.framework.base;

import com.framework.config.Config;
import com.framework.reporting.AllureReportUtils;
import com.framework.reporting.HtmlReportGenerator;
import com.framework.reporting.PerformanceMetrics;
import com.framework.utils.*;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Enhanced base test class with comprehensive lifecycle management and utilities.
 */
public class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());
    private Instant testStartTime;
    protected SoftAssert softAssert;

    @BeforeSuite(alwaysRun = true)
    public void globalSetUp() throws Exception {
        log.info("========================================");
        log.info("Starting Test Suite Execution");
        log.info("========================================");

        // Print configuration summary
        Config.get().printConfiguration();

        // Initialize Allure environment info
        AllureReportUtils.generateEnvironmentProperties();
        AllureReportUtils.generateCategories();
        AllureReportUtils.generateExecutorInfo();

        // Initialize HTML report
        HtmlReportGenerator.startSuite("MobileX Test Suite");

        // Take memory snapshot
        PerformanceMetrics.takeMemorySnapshot("suite_start");

        // Create driver session
        DriverManager.initDriver(Config.get().platform());

        log.info("Platform: {}", Config.get().platform());
        log.info("Device: {} ({})", Config.get().deviceName(), Config.get().udid());
        log.info("Appium Server: {}", Config.get().appiumServerUrl());
        log.info("Environment: {}", Config.get().env().toUpperCase());
    }

    @AfterSuite(alwaysRun = true)
    public void globalTearDown() {
        log.info("========================================");
        log.info("Completing Test Suite Execution");
        log.info("========================================");

        // Performance summary
        PerformanceMetrics.takeMemorySnapshot("suite_end");
        String perfSummary = PerformanceMetrics.generateSummaryReport();
        log.info(perfSummary);
        AllureReportUtils.attachText("Performance Summary", perfSummary);

        // Generate reports
        HtmlReportGenerator.endSuite();
        HtmlReportGenerator.generateReport();

        // Quit driver
        DriverManager.quitDriver();
    }

    @BeforeClass(alwaysRun = true)
    public void classSetUp() {
        log.info("--- Starting Test Class: {} ---", getClass().getSimpleName());
    }

    @AfterClass(alwaysRun = true)
    public void classTearDown() {
        log.info("--- Completed Test Class: {} ---", getClass().getSimpleName());
    }

    @BeforeMethod(alwaysRun = true)
    public void methodSetUp(Method method) {
        testStartTime = Instant.now();
        softAssert = new SoftAssert();
        AssertUtils.resetSoftAssert();

        log.info(">>> Starting Test: {}", method.getName());
        Allure.step("Test started: " + method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void methodTearDown(ITestResult result, Method method) {
        long duration = Duration.between(testStartTime, Instant.now()).toMillis();

        String status = switch (result.getStatus()) {
            case ITestResult.SUCCESS -> "PASSED";
            case ITestResult.FAILURE -> "FAILED";
            case ITestResult.SKIP -> "SKIPPED";
            default -> "UNKNOWN";
        };

        log.info("<<< Completed Test: {} - {} ({}ms)", method.getName(), status, duration);

        // Record for HTML report
        HtmlReportGenerator.recordTestResult(
                method.getName(),
                getClass().getSimpleName(),
                switch (result.getStatus()) {
                    case ITestResult.SUCCESS -> HtmlReportGenerator.Status.PASSED;
                    case ITestResult.FAILURE -> HtmlReportGenerator.Status.FAILED;
                    default -> HtmlReportGenerator.Status.SKIPPED;
                },
                duration,
                result.getThrowable() != null ? result.getThrowable().getMessage() : null
        );

        // Record performance metric
        PerformanceMetrics.recordActionTime("test_" + method.getName(), duration);
    }

    // ==================== Driver Getters ====================

    protected AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }

    protected AndroidDriver getAndroidDriver() {
        return DriverManager.getAndroidDriver();
    }

    protected IOSDriver getIOSDriver() {
        return DriverManager.getIOSDriver();
    }

    protected boolean isAndroid() {
        return DriverManager.isAndroid();
    }

    protected boolean isIOS() {
        return DriverManager.isIOS();
    }

    // ==================== Page Navigation ====================

    /**
     * Creates and returns a page object instance.
     */
    protected <T extends BasePage> T getPage(Class<T> pageClass) {
        try {
            return pageClass.getDeclaredConstructor(AppiumDriver.class).newInstance(getDriver());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create page: " + pageClass.getSimpleName(), e);
        }
    }

    // ==================== Allure Reporting ====================

    @Step("{stepDescription}")
    protected void step(String stepDescription) {
        log.info("Step: {}", stepDescription);
    }

    protected void attachScreenshot(String name) {
        ScreenshotUtils.attachToAllure(name);
    }

    protected void attachText(String name, String content) {
        AllureReportUtils.attachText(name, content);
    }

    protected void attachJson(String name, String json) {
        AllureReportUtils.attachJson(name, json);
    }

    // ==================== Test Utilities ====================

    /**
     * Waits for specified duration.
     */
    protected void waitFor(int seconds) {
        WaitUtils.hardWait(seconds * 1000);
    }

    /**
     * Gets test configuration.
     */
    protected Config getConfig() {
        return Config.get();
    }

    /**
     * Gets device information.
     */
    protected Map<String, String> getDeviceInfo() {
        return DeviceUtils.getDeviceInfo();
    }

    /**
     * Takes app to background.
     */
    protected void backgroundApp(int seconds) {
        AppUtils.backgroundApp(Duration.ofSeconds(seconds));
    }

    /**
     * Presses back button.
     */
    protected void pressBack() {
        AppUtils.pressBack();
    }

    /**
     * Hides keyboard if visible.
     */
    protected void hideKeyboard() {
        AppUtils.hideKeyboard();
    }

    /**
     * Generates random test data.
     */
    protected String randomString(int length) {
        return DataUtils.randomString(length);
    }

    protected String randomEmail() {
        return DataUtils.randomEmail();
    }

    protected String uniqueTestId() {
        return DataUtils.uniqueTestId();
    }
}
