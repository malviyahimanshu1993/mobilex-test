package com.framework.base;

import com.framework.config.Config;
import com.framework.reporting.PerformanceMetrics;
import com.framework.utils.AdvancedGestureUtils;
import com.framework.utils.ElementUtils;
import com.framework.utils.ScreenshotUtils;
import com.framework.utils.WaitUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced base page with comprehensive utilities and fluent interface.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());
    protected final AppiumDriver driver;
    private final String pageName;

    protected BasePage(AppiumDriver driver) {
        if (driver == null) {
            throw new IllegalStateException(
                "Driver is null. Ensure DriverManager.initDriver() was called in @BeforeSuite " +
                "and tests extend BaseTest class properly.");
        }
        this.driver = driver;
        this.pageName = getClass().getSimpleName();
        initPageFactory();
        log.debug("Initialized page: {}", pageName);
    }

    /**
     * Initializes page factory for @AndroidFindBy and @iOSXCUITFindBy annotations.
     */
    protected void initPageFactory() {
        PageFactory.initElements(new AppiumFieldDecorator(driver,
                Duration.ofSeconds(Config.get().explicitWaitSeconds())), this);
    }

    /**
     * Gets the page name (for logging and reporting).
     */
    public String getPageName() {
        return pageName;
    }

    /**
     * Waits for page to be fully loaded. Override in subclasses for specific logic.
     */
    public BasePage waitForPageLoad() {
        PerformanceMetrics.startTimer(pageName + "_load");
        // Default implementation - subclasses should override
        WaitUtils.waitForPageLoad();
        PerformanceMetrics.stopTimerAsPageLoad(pageName);
        return this;
    }

    /**
     * Verifies page is displayed. Override in subclasses.
     */
    public boolean isPageDisplayed() {
        return true;
    }

    /**
     * Takes a screenshot and attaches to report.
     */
    @Step("Capture screenshot: {name}")
    public BasePage takeScreenshot(String name) {
        ScreenshotUtils.attachToAllure(name);
        return this;
    }

    protected FluentWait<AppiumDriver> waitDefault() {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(Config.get().explicitWaitSeconds()))
                .pollingEvery(Duration.ofMillis(250))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }

    protected WebElement waitForVisible(By locator) {
        return waitDefault().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForVisible(AppiumBy locator) {
        return waitDefault().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return waitDefault().until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitForClickable(AppiumBy locator) {
        return waitDefault().until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        clickWithStaleRecovery(locator, 1);
    }

    protected void click(AppiumBy locator) {
        clickWithStaleRecovery(locator, 1);
    }

    protected void type(By locator, String text) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected void type(AppiumBy locator, String text) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected boolean waitForText(By locator, String text) {
        return waitDefault().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    protected boolean waitForInvisible(By locator) {
        return waitDefault().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected <T> T waitFor(ExpectedCondition<T> condition) {
        return waitDefault().until(condition);
    }

    /**
     * Utility method to print the current page source to stdout.
     * Can be invoked from tests/listeners after a class or method finishes.
     */
    public void printPageSource() {
        if (driver == null) return;
        try {
            String src = driver.getPageSource();
            log.debug("===== BEGIN PAGE SOURCE =====");
            log.debug(src);
            log.debug("===== END PAGE SOURCE =====");
        } catch (Exception e) {
            log.error("Failed to get page source: {}", e.getMessage());
        }
    }

    // ==================== Enhanced Methods ====================

    /**
     * Waits for element with custom timeout.
     */
    protected WebElement waitForVisible(By locator, int timeoutSeconds) {
        return ElementUtils.waitForVisible(locator, timeoutSeconds);
    }

    /**
     * Waits for element to be present in DOM.
     */
    protected WebElement waitForPresence(By locator) {
        return ElementUtils.waitForPresence(locator);
    }

    /**
     * Checks if element exists without waiting.
     */
    protected boolean exists(By locator) {
        return ElementUtils.exists(locator);
    }

    /**
     * Gets element count.
     */
    protected int getElementCount(By locator) {
        return ElementUtils.getElementCount(locator);
    }

    /**
     * Finds all elements matching locator.
     */
    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    /**
     * Safely gets element text.
     */
    protected String getText(WebElement element) {
        return ElementUtils.getText(element);
    }

    /**
     * Gets attribute value.
     */
    protected String getAttribute(WebElement element, String attribute) {
        return ElementUtils.getAttribute(element, attribute);
    }

    /**
     * Checks if element is displayed.
     */
    protected boolean isDisplayed(WebElement element) {
        return ElementUtils.isDisplayed(element);
    }

    /**
     * Clicks element with logging.
     */
    @Step("Click on element")
    protected void clickElement(WebElement element) {
        log.debug("Clicking element: {}", element);
        ElementUtils.safeClick(element);
    }

    /**
     * Types text with logging.
     */
    @Step("Type text: {text}")
    protected void typeText(WebElement element, String text) {
        log.debug("Typing text into element");
        ElementUtils.safeSendKeys(element, text);
    }

    /**
     * Clears field.
     */
    protected void clearField(WebElement element) {
        ElementUtils.clearField(element);
    }

    // ==================== Gesture Methods ====================

    /**
     * Swipes up on the screen.
     */
    @Step("Swipe up")
    protected void swipeUp() {
        AdvancedGestureUtils.swipe(AdvancedGestureUtils.SwipeDirection.UP);
    }

    /**
     * Swipes down on the screen.
     */
    @Step("Swipe down")
    protected void swipeDown() {
        AdvancedGestureUtils.swipe(AdvancedGestureUtils.SwipeDirection.DOWN);
    }

    /**
     * Swipes left on the screen.
     */
    @Step("Swipe left")
    protected void swipeLeft() {
        AdvancedGestureUtils.swipe(AdvancedGestureUtils.SwipeDirection.LEFT);
    }

    /**
     * Swipes right on the screen.
     */
    @Step("Swipe right")
    protected void swipeRight() {
        AdvancedGestureUtils.swipe(AdvancedGestureUtils.SwipeDirection.RIGHT);
    }

    /**
     * Long presses on element.
     */
    @Step("Long press element")
    protected void longPress(WebElement element) {
        AdvancedGestureUtils.longPressElement(element);
    }

    /**
     * Double taps on element.
     */
    @Step("Double tap element")
    protected void doubleTap(WebElement element) {
        AdvancedGestureUtils.doubleTapElement(element);
    }

    /**
     * Scrolls to text.
     */
    @Step("Scroll to text: {text}")
    protected WebElement scrollToText(String text) {
        return ElementUtils.scrollToText(text);
    }

    // ==================== Fluent Methods ====================

    /**
     * Adds Allure step with description.
     */
    protected BasePage step(String stepDescription) {
        Allure.step(stepDescription);
        log.info("Step: {}", stepDescription);
        return this;
    }

    /**
     * Waits for specified milliseconds.
     */
    protected BasePage pause(int milliseconds) {
        WaitUtils.hardWait(milliseconds);
        return this;
    }

    /**
     * Returns Optional element.
     */
    protected Optional<WebElement> findOptional(By locator) {
        return ElementUtils.findElementOptional(locator);
    }

    private void clickWithStaleRecovery(By locator, int retry) {
        try {
            waitForClickable(locator).click();
        } catch (StaleElementReferenceException e) {
            if (retry <= 0) throw e;
            waitDefault().until(d -> true);
            clickWithStaleRecovery(locator, retry - 1);
        }
    }

    private void clickWithStaleRecovery(AppiumBy locator, int retry) {
        try {
            waitForClickable(locator).click();
        } catch (StaleElementReferenceException e) {
            if (retry <= 0) throw e;
            waitDefault().until(d -> true);
            clickWithStaleRecovery(locator, retry - 1);
        }
    }
}
