package com.framework.utils;

import com.framework.base.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Advanced wait utilities providing various wait strategies.
 */
public final class WaitUtils {

    private static final int DEFAULT_TIMEOUT = 10;
    private static final int DEFAULT_POLL_MS = 250;
    private static final int SHORT_TIMEOUT = 5;
    private static final int LONG_TIMEOUT = 30;

    private WaitUtils() {
    }

    /**
     * Creates default WebDriverWait.
     */
    public static WebDriverWait defaultWait() {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    /**
     * Creates WebDriverWait with custom timeout.
     */
    public static WebDriverWait waitFor(int seconds) {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(seconds));
    }

    /**
     * Creates FluentWait with comprehensive exception ignoring.
     */
    public static FluentWait<AppiumDriver> fluentWait(int timeoutSeconds, int pollingMillis) {
        return new FluentWait<>(DriverManager.getDriver())
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(pollingMillis))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    /**
     * Hard wait (use sparingly - only for debugging or special cases).
     */
    public static void hardWait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Wait until a custom condition is true.
     */
    public static <T> T waitUntil(ExpectedCondition<T> condition) {
        return defaultWait().until(condition);
    }

    /**
     * Wait until a custom condition is true with timeout.
     */
    public static <T> T waitUntil(ExpectedCondition<T> condition, int timeoutSeconds) {
        return waitFor(timeoutSeconds).until(condition);
    }

    /**
     * Wait for page to load completely (DOM ready state).
     */
    public static void waitForPageLoad() {
        waitFor(LONG_TIMEOUT).until(driver -> {
            try {
                return ((AppiumDriver) driver).getPageSource() != null;
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Wait for element count to be a specific value.
     */
    public static boolean waitForElementCount(By locator, int count) {
        java.util.List<org.openqa.selenium.WebElement> elements =
            defaultWait().until(ExpectedConditions.numberOfElementsToBe(locator, count));
        return elements != null && elements.size() == count;
    }

    /**
     * Wait for element count to be more than value.
     */
    public static boolean waitForElementCountMoreThan(By locator, int count) {
        java.util.List<org.openqa.selenium.WebElement> elements =
            defaultWait().until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, count));
        return elements != null && elements.size() > count;
    }

    /**
     * Wait for element count to be less than value.
     */
    public static boolean waitForElementCountLessThan(By locator, int count) {
        java.util.List<org.openqa.selenium.WebElement> elements =
            defaultWait().until(ExpectedConditions.numberOfElementsToBeLessThan(locator, count));
        return elements != null && elements.size() < count;
    }

    /**
     * Wait for any of multiple elements to be visible.
     */
    public static WebElement waitForAnyVisible(By... locators) {
        return defaultWait().until(driver -> {
            for (By locator : locators) {
                try {
                    WebElement element = driver.findElement(locator);
                    if (element.isDisplayed()) {
                        return element;
                    }
                } catch (NoSuchElementException ignored) {
                }
            }
            return null;
        });
    }

    /**
     * Wait for all elements to be visible.
     */
    public static boolean waitForAllVisible(By... locators) {
        return defaultWait().until(driver -> {
            for (By locator : locators) {
                try {
                    WebElement element = driver.findElement(locator);
                    if (!element.isDisplayed()) {
                        return false;
                    }
                } catch (NoSuchElementException e) {
                    return false;
                }
            }
            return true;
        });
    }

    /**
     * Wait for element to have specific text.
     */
    public static boolean waitForExactText(By locator, String text) {
        return defaultWait().until(ExpectedConditions.textToBe(locator, text));
    }

    /**
     * Wait for element text to match pattern.
     */
    public static boolean waitForTextMatches(By locator, String regex) {
        return defaultWait().until(ExpectedConditions.textMatches(locator, java.util.regex.Pattern.compile(regex)));
    }

    /**
     * Wait for element to be stale (removed from DOM).
     */
    public static boolean waitForStale(WebElement element) {
        return defaultWait().until(ExpectedConditions.stalenessOf(element));
    }

    /**
     * Wait for frame and switch to it.
     */
    public static void waitAndSwitchToFrame(By locator) {
        defaultWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    /**
     * Wait with polling using custom function.
     */
    public static <T> T waitWithPolling(Function<AppiumDriver, T> condition, int timeoutSeconds, int pollingMillis) {
        return fluentWait(timeoutSeconds, pollingMillis).until(condition);
    }

    /**
     * Wait for condition with exponential backoff.
     */
    public static <T> T waitWithBackoff(Supplier<T> condition, int maxAttempts, int initialDelayMs) {
        int delay = initialDelayMs;
        Exception lastException = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                T result = condition.get();
                if (result != null && !(result instanceof Boolean && !((Boolean) result))) {
                    return result;
                }
            } catch (Exception e) {
                lastException = e;
            }

            hardWait(delay);
            delay *= 2; // Exponential backoff
        }

        if (lastException != null) {
            throw new RuntimeException("Condition not met after " + maxAttempts + " attempts", lastException);
        }
        throw new RuntimeException("Condition not met after " + maxAttempts + " attempts");
    }

    /**
     * Wait for loading indicator to disappear.
     */
    public static void waitForLoadingToComplete(By loadingIndicator) {
        waitFor(SHORT_TIMEOUT).until(ExpectedConditions.invisibilityOfElementLocated(loadingIndicator));
    }

    /**
     * Wait for loading indicator to appear and then disappear.
     */
    public static void waitForLoadingCycle(By loadingIndicator, int maxWaitSeconds) {
        try {
            // First wait for it to appear (short timeout - may already be gone)
            waitFor(2).until(ExpectedConditions.visibilityOfElementLocated(loadingIndicator));
        } catch (Exception ignored) {
            // Loading might have been too fast
        }
        // Then wait for it to disappear
        waitFor(maxWaitSeconds).until(ExpectedConditions.invisibilityOfElementLocated(loadingIndicator));
    }

    /**
     * Wait for element attribute to change.
     */
    public static boolean waitForAttributeChange(WebElement element, String attribute, String initialValue) {
        return defaultWait().until(driver -> {
            String currentValue = element.getAttribute(attribute);
            return currentValue != null && !currentValue.equals(initialValue);
        });
    }

    /**
     * Wait for toast/snackbar message (common in mobile apps).
     */
    public static WebElement waitForToast(String toastText) {
        // Android toast - may vary by app
        return waitFor(SHORT_TIMEOUT).until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(@text,'" + toastText + "')]")));
    }

    /**
     * Fluent wait with custom message for better debugging.
     */
    public static <T> T waitWithMessage(ExpectedCondition<T> condition, int timeoutSeconds, String message) {
        return fluentWait(timeoutSeconds, DEFAULT_POLL_MS)
                .withMessage(message)
                .until(condition);
    }
}

