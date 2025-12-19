package com.framework.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.interactions.PointerInput.Origin;

import java.time.Duration;
import java.util.Collections;

public class GestureUtils {

    private static final int DEFAULT_DURATION = 500; // ms

    public static void swipeUp(AppiumDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = (int) (size.height * 0.2);
        performSwipe(driver, startX, startY, startX, endY, DEFAULT_DURATION);
    }

    public static void swipeDown(AppiumDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.2);
        int endY = (int) (size.height * 0.8);
        performSwipe(driver, startX, startY, startX, endY, DEFAULT_DURATION);
    }

    public static void swipeLeft(AppiumDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int startY = size.height / 2;
        int startX = (int) (size.width * 0.8);
        int endX = (int) (size.width * 0.2);
        performSwipe(driver, startX, startY, endX, startY, DEFAULT_DURATION);
    }

    public static void swipeRight(AppiumDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int startY = size.height / 2;
        int startX = (int) (size.width * 0.2);
        int endX = (int) (size.width * 0.8);
        performSwipe(driver, startX, startY, endX, startY, DEFAULT_DURATION);
    }

    public static void scrollToElement(AppiumDriver driver, WebElement element) {
        // Scroll by doing a swipe from bottom to the element center
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY = element.getRect().y + element.getRect().height / 2;
        performSwipe(driver, startX, startY, startX, endY, DEFAULT_DURATION);
    }

    private static void performSwipe(AppiumDriver driver, int startX, int startY, int endX, int endY, int durationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        // Move to start
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(0), Origin.viewport(), startX, startY));
        // Finger down
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        // Move to end (duration controls speed)
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMs), Origin.viewport(), endX, endY));
        // Finger up
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }
}

