package com.framework.tests;

import com.framework.annotations.*;
import com.framework.base.BaseTest;
import com.framework.listeners.DynamicDataProvider;
import com.framework.reporting.AllureReportUtils;
import com.framework.reporting.PerformanceMetrics;
import com.framework.utils.*;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Example test class demonstrating all enhanced framework features.
 */
@Epic("Framework Demo")
@Feature("Advanced Features")
@TestInfo(author = "Framework Team", priority = "High", component = "Demo")
@TestCategory(type = TestCategory.TestType.SMOKE, severity = TestCategory.Severity.NORMAL,
              feature = "Framework Demo", tags = {"demo", "smoke"})
public class ExampleEnhancedTest extends BaseTest {

    @Test(description = "Demonstrates performance tracking")
    @Story("Performance Metrics")
    @PerformanceTest(maxDurationMs = 10000, logMetrics = true)
    @Screenshot(value = {Screenshot.CaptureMode.BEFORE, Screenshot.CaptureMode.AFTER})
    public void testPerformanceMetrics() {
        step("Starting performance test");

        // Track page load
        PerformanceMetrics.startTimer("homepage_load");
        WaitUtils.hardWait(500); // Simulate page load
        PerformanceMetrics.stopTimerAsPageLoad("homepage");

        // Track action
        PerformanceMetrics.startTimer("button_click");
        WaitUtils.hardWait(200); // Simulate action
        PerformanceMetrics.stopTimerAsAction("click_login");

        step("Performance metrics recorded successfully");
    }

    @Test(description = "Demonstrates soft assertions")
    @Story("Soft Assertions")
    public void testSoftAssertions() {
        step("Running soft assertions");

        // Soft assertions - won't fail immediately
        AssertUtils.softAssertEquals("actual", "actual", "Strings should match");
        AssertUtils.softAssertTrue(true, "Condition should be true");
        AssertUtils.softAssertNotNull("not null", "Object should not be null");

        // Assert all at the end
        AssertUtils.assertAll();

        step("All soft assertions passed");
    }

    @Test(description = "Demonstrates test data generation")
    @Story("Data Generation")
    public void testDataGeneration() {
        step("Generating test data");

        // Generate various types of test data
        String firstName = TestDataGenerator.firstName();
        String email = TestDataGenerator.email();
        String phone = TestDataGenerator.cellPhone();
        String password = TestDataGenerator.password(10, 16);

        log.info("Generated: name={}, email={}, phone={}", firstName, email, phone);

        // Using builder pattern
        Map<String, String> userData = TestDataGenerator.builder()
                .withName()
                .withEmail()
                .withPhone()
                .withCredentials()
                .build();

        AllureReportUtils.attachJson("Generated User Data", DataUtils.toJson(userData));

        Assert.assertNotNull(userData.get("firstName"));
        Assert.assertNotNull(userData.get("email"));

        step("Test data generated successfully");
    }

    @Test(description = "Demonstrates advanced assertions")
    @Story("Advanced Assertions")
    public void testAdvancedAssertions() {
        step("Running advanced assertions");

        // String assertions
        AssertUtils.assertContains("Hello World", "World", "Should contain 'World'");
        AssertUtils.assertStartsWith("Hello World", "Hello", "Should start with 'Hello'");
        AssertUtils.assertMatches("test123", "\\w+\\d+", "Should match alphanumeric pattern");

        // Numeric assertions
        AssertUtils.assertGreaterThan(10, 5, "10 should be greater than 5");
        AssertUtils.assertInRange(7, 1, 10, "7 should be in range 1-10");

        step("Advanced assertions passed");
    }

    @Test(description = "Demonstrates Allure reporting features")
    @Story("Allure Reporting")
    @Severity(SeverityLevel.CRITICAL)
    @Link(name = "Documentation", url = "https://docs.example.com")
    public void testAllureReporting() {
        AllureReportUtils.setDescription("This test demonstrates Allure reporting features");

        AllureReportUtils.step("Step 1: Initialize test environment");
        log.info("Environment initialized");

        AllureReportUtils.step("Step 2: Perform test actions");
        String testData = "{\"key\": \"value\"}";
        AllureReportUtils.attachJson("Test Data", testData);

        AllureReportUtils.step("Step 3: Verify results");
        AllureReportUtils.attachText("Verification Result", "All checks passed");

        AllureReportUtils.addIssue("JIRA-123");
        AllureReportUtils.addTmsLink("TC-456");
    }

    @Test(description = "Demonstrates device utilities")
    @Story("Device Management")
    @RequiresFeature(value = {RequiresFeature.Feature.ANDROID}, skipIfUnavailable = true)
    public void testDeviceUtilities() {
        step("Getting device information");

        Map<String, String> deviceInfo = DeviceUtils.getDeviceInfo();
        log.info("Device Info: {}", deviceInfo);

        String androidVersion = DeviceUtils.getAndroidVersion();
        String manufacturer = DeviceUtils.getManufacturer();
        String model = DeviceUtils.getModel();

        log.info("Android: {}, Manufacturer: {}, Model: {}", androidVersion, manufacturer, model);

        AllureReportUtils.attachJson("Device Information", DataUtils.toJson(deviceInfo));

        step("Device information retrieved");
    }

    @Test(description = "Demonstrates wait utilities")
    @Story("Wait Strategies")
    public void testWaitUtilities() {
        step("Testing wait utilities");

        // Hard wait (use sparingly)
        WaitUtils.hardWait(100);

        // Wait with exponential backoff
        String result = WaitUtils.waitWithBackoff(
                () -> "success",
                3,
                100
        );
        Assert.assertEquals(result, "success");

        step("Wait utilities working correctly");
    }

    @Test(description = "Demonstrates flaky test handling")
    @Story("Retry Mechanism")
    @FlakyTest(maxRetries = 3, delayMs = 500, reason = "Demo of retry mechanism", bugId = "BUG-789")
    public void testFlakyTestHandling() {
        step("This test demonstrates retry mechanism");

        // This test will pass
        Assert.assertTrue(true, "Test should pass");

        step("Test completed successfully");
    }

    @Test(description = "Demonstrates random data utilities")
    @Story("Random Data")
    public void testRandomDataUtilities() {
        step("Testing random data utilities");

        // Basic random data
        String randomStr = DataUtils.randomString(10);
        String randomEmail = DataUtils.randomEmail();
        String randomPhone = DataUtils.randomPhoneNumber();
        int randomNum = DataUtils.randomNumber(1, 100);

        log.info("Random string: {}", randomStr);
        log.info("Random email: {}", randomEmail);
        log.info("Random phone: {}", randomPhone);
        log.info("Random number: {}", randomNum);

        Assert.assertEquals(randomStr.length(), 10);
        Assert.assertTrue(randomEmail.contains("@"));
        Assert.assertTrue(randomNum >= 1 && randomNum <= 100);

        step("Random data generation working");
    }

    @Test(description = "Demonstrates unique identifiers")
    @Story("Unique IDs")
    public void testUniqueIdentifiers() {
        step("Generating unique identifiers");

        String testId1 = DataUtils.uniqueTestId();
        String testId2 = DataUtils.uniqueTestId();

        log.info("Test ID 1: {}", testId1);
        log.info("Test ID 2: {}", testId2);

        // Each call should produce unique ID
        Assert.assertNotEquals(testId1, testId2);

        step("Unique identifiers generated");
    }

    @Test(dataProvider = "inlineData", dataProviderClass = DynamicDataProvider.class,
          description = "Demonstrates data-driven testing with inline data")
    @Story("Data Driven Testing")
    public void testDataDrivenInline(String username, String password) {
        step("Testing with username: " + username);

        log.info("Login test with: {} / {}", username, password);
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);

        step("Data-driven test iteration complete");
    }
}

