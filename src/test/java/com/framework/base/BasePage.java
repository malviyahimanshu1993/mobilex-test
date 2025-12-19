package com.framework.base;

import com.framework.config.Config;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;

public abstract class BasePage {

    protected final AppiumDriver driver;

    protected BasePage(AppiumDriver driver) {
        this.driver = driver;
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
