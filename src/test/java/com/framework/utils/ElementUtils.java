package com.framework.utils;

import com.framework.base.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Element utilities for mobile automation.
 */
public final class ElementUtils {

    private static final int DEFAULT_TIMEOUT = 10;
    private static final int DEFAULT_POLL_MS = 250;
    private static final int MAX_RETRIES = 3;

    private ElementUtils() {
    }

    public static FluentWait<AppiumDriver> createFluentWait() {
        return createFluentWait(DEFAULT_TIMEOUT);
    }

    public static FluentWait<AppiumDriver> createFluentWait(int timeoutSeconds) {
        return new FluentWait<>(DriverManager.getDriver())
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofMillis(DEFAULT_POLL_MS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    public static String getText(WebElement element) {
        return retryOnStale(() -> element.getText(), MAX_RETRIES);
    }

    public static String getAttribute(WebElement element, String attributeName) {
        return retryOnStale(() -> element.getAttribute(attributeName), MAX_RETRIES);
    }

    public static boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public static WebElement waitForPresence(By locator) {
        return createFluentWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static WebElement waitForPresence(By locator, int timeoutSeconds) {
        return createFluentWait(timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public static WebElement waitForVisible(By locator) {
        return createFluentWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForVisible(By locator, int timeoutSeconds) {
        return createFluentWait(timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator) {
        return createFluentWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForClickable(By locator, int timeoutSeconds) {
        return createFluentWait(timeoutSeconds).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean waitForInvisible(By locator) {
        return createFluentWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static List<WebElement> findElements(By locator) {
        return DriverManager.getDriver().findElements(locator);
    }

    public static Optional<WebElement> findElementOptional(By locator) {
        List<WebElement> elements = findElements(locator);
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(0));
    }

    public static boolean exists(By locator) {
        return !findElements(locator).isEmpty();
    }

    public static int getElementCount(By locator) {
        return findElements(locator).size();
    }

    public static void safeClick(WebElement element) {
        retryOnStale(() -> {
            element.click();
            return null;
        }, MAX_RETRIES);
    }

    public static void safeClick(By locator) {
        WebElement element = waitForClickable(locator);
        safeClick(element);
    }

    public static void safeSendKeys(WebElement element, String text) {
        retryOnStale(() -> {
            element.clear();
            element.sendKeys(text);
            return null;
        }, MAX_RETRIES);
    }

    public static void safeSendKeys(By locator, String text) {
        WebElement element = waitForVisible(locator);
        safeSendKeys(element, text);
    }

    public static void clearField(WebElement element) {
        retryOnStale(() -> {
            element.clear();
            return null;
        }, MAX_RETRIES);
    }

    public static WebElement scrollToText(String text) {
        return DriverManager.getDriver().findElement(
                AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(" +
                                "new UiSelector().textContains(\"" + text + "\"))"));
    }

    private static <T> T retryOnStale(Supplier<T> action, int retries) {
        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                return action.get();
            } catch (StaleElementReferenceException e) {
                if (attempt == retries) throw e;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("Unexpected retry state");
    }
}

