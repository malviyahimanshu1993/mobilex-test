package com.framework.utils;

import com.framework.base.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * Advanced gesture utilities for mobile automation.
 * Includes complex gestures like pinch, zoom, long press, double tap, etc.
 */
public final class AdvancedGestureUtils {

    private static final Logger log = LogManager.getLogger(AdvancedGestureUtils.class);
    private static final int DEFAULT_DURATION = 500;
    private static final int QUICK_DURATION = 100;
    private static final int LONG_PRESS_DURATION = 1500;

    private AdvancedGestureUtils() {
    }

    /**
     * Performs a tap at specific coordinates.
     */
    public static void tap(int x, int y) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) return;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);

        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(new Pause(finger, Duration.ofMillis(50)));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(tap));
        log.debug("Tapped at coordinates: ({}, {})", x, y);
    }

    /**
     * Performs a tap on element center.
     */
    public static void tapElement(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int centerX = location.getX() + size.getWidth() / 2;
        int centerY = location.getY() + size.getHeight() / 2;
        tap(centerX, centerY);
    }

    /**
     * Performs a double tap at specific coordinates.
     */
    public static void doubleTap(int x, int y) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) return;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence doubleTap = new Sequence(finger, 1);

        doubleTap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        doubleTap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        doubleTap.addAction(new Pause(finger, Duration.ofMillis(50)));
        doubleTap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        doubleTap.addAction(new Pause(finger, Duration.ofMillis(100)));
        doubleTap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        doubleTap.addAction(new Pause(finger, Duration.ofMillis(50)));
        doubleTap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(doubleTap));
        log.debug("Double tapped at coordinates: ({}, {})", x, y);
    }

    /**
     * Performs a double tap on element.
     */
    public static void doubleTapElement(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int centerX = location.getX() + size.getWidth() / 2;
        int centerY = location.getY() + size.getHeight() / 2;
        doubleTap(centerX, centerY);
    }

    /**
     * Performs a long press at specific coordinates.
     */
    public static void longPress(int x, int y) {
        longPress(x, y, LONG_PRESS_DURATION);
    }

    /**
     * Performs a long press with custom duration.
     */
    public static void longPress(int x, int y, int durationMs) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) return;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence longPress = new Sequence(finger, 1);

        longPress.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        longPress.addAction(new Pause(finger, Duration.ofMillis(durationMs)));
        longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(longPress));
        log.debug("Long pressed at coordinates: ({}, {}) for {} ms", x, y, durationMs);
    }

    /**
     * Performs a long press on element.
     */
    public static void longPressElement(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        int centerX = location.getX() + size.getWidth() / 2;
        int centerY = location.getY() + size.getHeight() / 2;
        longPress(centerX, centerY);
    }

    /**
     * Performs a swipe gesture in specified direction.
     */
    public static void swipe(SwipeDirection direction) {
        swipe(direction, 0.6);
    }

    /**
     * Performs a swipe with custom distance.
     */
    public static void swipe(SwipeDirection direction, double distance) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) return;

        Dimension size = driver.manage().window().getSize();
        int centerX = size.getWidth() / 2;
        int centerY = size.getHeight() / 2;

        int startX, startY, endX, endY;

        switch (direction) {
            case UP:
                startX = centerX;
                startY = (int) (size.getHeight() * (0.5 + distance / 2));
                endX = centerX;
                endY = (int) (size.getHeight() * (0.5 - distance / 2));
                break;
            case DOWN:
                startX = centerX;
                startY = (int) (size.getHeight() * (0.5 - distance / 2));
                endX = centerX;
                endY = (int) (size.getHeight() * (0.5 + distance / 2));
                break;
            case LEFT:
                startX = (int) (size.getWidth() * (0.5 + distance / 2));
                startY = centerY;
                endX = (int) (size.getWidth() * (0.5 - distance / 2));
                endY = centerY;
                break;
            case RIGHT:
                startX = (int) (size.getWidth() * (0.5 - distance / 2));
                startY = centerY;
                endX = (int) (size.getWidth() * (0.5 + distance / 2));
                endY = centerY;
                break;
            default:
                return;
        }

        performSwipe(startX, startY, endX, endY, DEFAULT_DURATION);
        log.debug("Swiped {} with distance {}", direction, distance);
    }

    /**
     * Drags from one point to another.
     */
    public static void dragAndDrop(int startX, int startY, int endX, int endY) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) return;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence drag = new Sequence(finger, 1);

        drag.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        drag.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        drag.addAction(new Pause(finger, Duration.ofMillis(500)));
        drag.addAction(finger.createPointerMove(Duration.ofMillis(DEFAULT_DURATION),
                PointerInput.Origin.viewport(), endX, endY));
        drag.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(drag));
        log.debug("Dragged from ({}, {}) to ({}, {})", startX, startY, endX, endY);
    }

    /**
     * Performs a pinch gesture (zoom out).
     */
    public static void pinch(int centerX, int centerY, double scale) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) return;

        int offset = 200;
        int moveBy = (int) (offset * scale);

        PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
        PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");

        Sequence pinch1 = new Sequence(finger1, 0);
        pinch1.addAction(finger1.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                centerX, centerY - offset));
        pinch1.addAction(finger1.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        pinch1.addAction(finger1.createPointerMove(Duration.ofMillis(DEFAULT_DURATION),
                PointerInput.Origin.viewport(), centerX, centerY - offset + moveBy));
        pinch1.addAction(finger1.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        Sequence pinch2 = new Sequence(finger2, 0);
        pinch2.addAction(finger2.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                centerX, centerY + offset));
        pinch2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        pinch2.addAction(finger2.createPointerMove(Duration.ofMillis(DEFAULT_DURATION),
                PointerInput.Origin.viewport(), centerX, centerY + offset - moveBy));
        pinch2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Arrays.asList(pinch1, pinch2));
        log.debug("Pinch gesture performed at center: ({}, {})", centerX, centerY);
    }

    /**
     * Performs a zoom gesture (pinch out).
     */
    public static void zoom(int centerX, int centerY, double scale) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) return;

        int startOffset = 50;
        int endOffset = (int) (startOffset * scale);

        PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
        PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");

        Sequence zoom1 = new Sequence(finger1, 0);
        zoom1.addAction(finger1.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                centerX, centerY - startOffset));
        zoom1.addAction(finger1.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        zoom1.addAction(finger1.createPointerMove(Duration.ofMillis(DEFAULT_DURATION),
                PointerInput.Origin.viewport(), centerX, centerY - endOffset));
        zoom1.addAction(finger1.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        Sequence zoom2 = new Sequence(finger2, 0);
        zoom2.addAction(finger2.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(),
                centerX, centerY + startOffset));
        zoom2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        zoom2.addAction(finger2.createPointerMove(Duration.ofMillis(DEFAULT_DURATION),
                PointerInput.Origin.viewport(), centerX, centerY + endOffset));
        zoom2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Arrays.asList(zoom1, zoom2));
        log.debug("Zoom gesture performed at center: ({}, {})", centerX, centerY);
    }

    private static void performSwipe(int startX, int startY, int endX, int endY, int durationMs) {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) return;

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMs),
                PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    /**
     * Swipe direction enum.
     */
    public enum SwipeDirection {
        UP, DOWN, LEFT, RIGHT
    }
}

