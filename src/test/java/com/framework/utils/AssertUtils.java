package com.framework.utils;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Enhanced assertion utilities with soft assertions, retries, and detailed reporting.
 */
public final class AssertUtils {

    private static final Logger log = LogManager.getLogger(AssertUtils.class);
    private static final ThreadLocal<EnhancedSoftAssert> SOFT_ASSERT = ThreadLocal.withInitial(EnhancedSoftAssert::new);

    private AssertUtils() {
    }

    // ==================== Soft Assert Management ====================

    /**
     * Gets the current thread's soft assert instance.
     */
    public static EnhancedSoftAssert getSoftAssert() {
        return SOFT_ASSERT.get();
    }

    /**
     * Resets soft assertions for current thread.
     */
    public static void resetSoftAssert() {
        SOFT_ASSERT.set(new EnhancedSoftAssert());
    }

    /**
     * Asserts all soft assertions and resets.
     */
    public static void assertAll() {
        try {
            getSoftAssert().assertAll();
        } finally {
            resetSoftAssert();
        }
    }

    /**
     * Asserts all with custom message.
     */
    public static void assertAll(String message) {
        try {
            getSoftAssert().assertAll(message);
        } finally {
            resetSoftAssert();
        }
    }

    // ==================== Soft Assertions ====================

    /**
     * Soft assert equals.
     */
    public static void softAssertEquals(Object actual, Object expected, String message) {
        getSoftAssert().assertEquals(actual, expected, message);
        logAssertion("assertEquals", actual, expected, message, Objects.equals(actual, expected));
    }

    /**
     * Soft assert not equals.
     */
    public static void softAssertNotEquals(Object actual, Object expected, String message) {
        getSoftAssert().assertNotEquals(actual, expected, message);
        logAssertion("assertNotEquals", actual, expected, message, !Objects.equals(actual, expected));
    }

    /**
     * Soft assert true.
     */
    public static void softAssertTrue(boolean condition, String message) {
        getSoftAssert().assertTrue(condition, message);
        logAssertion("assertTrue", condition, true, message, condition);
    }

    /**
     * Soft assert false.
     */
    public static void softAssertFalse(boolean condition, String message) {
        getSoftAssert().assertFalse(condition, message);
        logAssertion("assertFalse", condition, false, message, !condition);
    }

    /**
     * Soft assert null.
     */
    public static void softAssertNull(Object object, String message) {
        getSoftAssert().assertNull(object, message);
        logAssertion("assertNull", object, null, message, object == null);
    }

    /**
     * Soft assert not null.
     */
    public static void softAssertNotNull(Object object, String message) {
        getSoftAssert().assertNotNull(object, message);
        logAssertion("assertNotNull", object, "not null", message, object != null);
    }

    // ==================== Enhanced Hard Assertions ====================

    /**
     * Assert with screenshot on failure.
     */
    public static void assertEqualsWithScreenshot(Object actual, Object expected, String message) {
        boolean passed = Objects.equals(actual, expected);
        if (!passed) {
            ScreenshotUtils.attachToAllure("assertion-failure");
        }
        logAssertion("assertEquals", actual, expected, message, passed);
        org.testng.Assert.assertEquals(actual, expected, message);
    }

    /**
     * Assert true with screenshot on failure.
     */
    public static void assertTrueWithScreenshot(boolean condition, String message) {
        if (!condition) {
            ScreenshotUtils.attachToAllure("assertion-failure");
        }
        logAssertion("assertTrue", condition, true, message, condition);
        org.testng.Assert.assertTrue(condition, message);
    }

    /**
     * Assert with retry.
     */
    public static void assertWithRetry(Supplier<Boolean> condition, String message, int maxRetries, int delayMs) {
        boolean result = false;
        int attempts = 0;

        while (attempts < maxRetries && !result) {
            try {
                result = condition.get();
            } catch (Exception e) {
                log.debug("Assertion attempt {} failed: {}", attempts + 1, e.getMessage());
            }

            if (!result && attempts < maxRetries - 1) {
                WaitUtils.hardWait(delayMs);
            }
            attempts++;
        }

        logAssertion("assertWithRetry", result, true, message + " (after " + attempts + " attempts)", result);
        org.testng.Assert.assertTrue(result, message);
    }

    // ==================== Collection Assertions ====================

    /**
     * Assert collection contains item.
     */
    public static <T> void assertContains(Collection<T> collection, T item, String message) {
        boolean contains = collection != null && collection.contains(item);
        logAssertion("assertContains", collection, "contains " + item, message, contains);
        org.testng.Assert.assertTrue(contains, message);
    }

    /**
     * Assert collection does not contain item.
     */
    public static <T> void assertNotContains(Collection<T> collection, T item, String message) {
        boolean notContains = collection == null || !collection.contains(item);
        logAssertion("assertNotContains", collection, "not contains " + item, message, notContains);
        org.testng.Assert.assertTrue(notContains, message);
    }

    /**
     * Assert collection is empty.
     */
    public static void assertEmpty(Collection<?> collection, String message) {
        boolean empty = collection == null || collection.isEmpty();
        logAssertion("assertEmpty", collection != null ? collection.size() : 0, 0, message, empty);
        org.testng.Assert.assertTrue(empty, message);
    }

    /**
     * Assert collection is not empty.
     */
    public static void assertNotEmpty(Collection<?> collection, String message) {
        boolean notEmpty = collection != null && !collection.isEmpty();
        logAssertion("assertNotEmpty", collection != null ? collection.size() : 0, ">0", message, notEmpty);
        org.testng.Assert.assertTrue(notEmpty, message);
    }

    /**
     * Assert collection size.
     */
    public static void assertSize(Collection<?> collection, int expectedSize, String message) {
        int actualSize = collection != null ? collection.size() : 0;
        boolean match = actualSize == expectedSize;
        logAssertion("assertSize", actualSize, expectedSize, message, match);
        org.testng.Assert.assertEquals(actualSize, expectedSize, message);
    }

    // ==================== String Assertions ====================

    /**
     * Assert string contains substring.
     */
    public static void assertContains(String actual, String substring, String message) {
        boolean contains = actual != null && actual.contains(substring);
        logAssertion("assertContains", actual, "contains '" + substring + "'", message, contains);
        org.testng.Assert.assertTrue(contains, message);
    }

    /**
     * Assert string starts with prefix.
     */
    public static void assertStartsWith(String actual, String prefix, String message) {
        boolean startsWith = actual != null && actual.startsWith(prefix);
        logAssertion("assertStartsWith", actual, "starts with '" + prefix + "'", message, startsWith);
        org.testng.Assert.assertTrue(startsWith, message);
    }

    /**
     * Assert string ends with suffix.
     */
    public static void assertEndsWith(String actual, String suffix, String message) {
        boolean endsWith = actual != null && actual.endsWith(suffix);
        logAssertion("assertEndsWith", actual, "ends with '" + suffix + "'", message, endsWith);
        org.testng.Assert.assertTrue(endsWith, message);
    }

    /**
     * Assert string matches regex.
     */
    public static void assertMatches(String actual, String regex, String message) {
        boolean matches = actual != null && actual.matches(regex);
        logAssertion("assertMatches", actual, "matches '" + regex + "'", message, matches);
        org.testng.Assert.assertTrue(matches, message);
    }

    /**
     * Assert string equals ignoring case.
     */
    public static void assertEqualsIgnoreCase(String actual, String expected, String message) {
        boolean equals = actual != null && actual.equalsIgnoreCase(expected);
        logAssertion("assertEqualsIgnoreCase", actual, expected, message, equals);
        org.testng.Assert.assertTrue(equals, message);
    }

    // ==================== Numeric Assertions ====================

    /**
     * Assert number is greater than.
     */
    public static void assertGreaterThan(Number actual, Number expected, String message) {
        boolean greater = actual.doubleValue() > expected.doubleValue();
        logAssertion("assertGreaterThan", actual, ">" + expected, message, greater);
        org.testng.Assert.assertTrue(greater, message);
    }

    /**
     * Assert number is less than.
     */
    public static void assertLessThan(Number actual, Number expected, String message) {
        boolean less = actual.doubleValue() < expected.doubleValue();
        logAssertion("assertLessThan", actual, "<" + expected, message, less);
        org.testng.Assert.assertTrue(less, message);
    }

    /**
     * Assert number is in range.
     */
    public static void assertInRange(Number actual, Number min, Number max, String message) {
        boolean inRange = actual.doubleValue() >= min.doubleValue() && actual.doubleValue() <= max.doubleValue();
        logAssertion("assertInRange", actual, "[" + min + ", " + max + "]", message, inRange);
        org.testng.Assert.assertTrue(inRange, message);
    }

    // ==================== Helper Methods ====================

    private static void logAssertion(String assertType, Object actual, Object expected, String message, boolean passed) {
        String status = passed ? "✓ PASSED" : "✗ FAILED";
        String logMessage = String.format("%s: %s - Actual: '%s', Expected: '%s' - %s",
                assertType, message, actual, expected, status);

        if (passed) {
            log.info(logMessage);
        } else {
            log.error(logMessage);
        }

        Allure.step(logMessage);
    }

    /**
     * Enhanced SoftAssert with failure tracking.
     */
    public static class EnhancedSoftAssert extends SoftAssert {
        private final List<String> failures = new ArrayList<>();

        @Override
        public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
            String failure = assertCommand.getMessage() + ": " + ex.getMessage();
            failures.add(failure);
            log.error("Soft assertion failed: {}", failure);
            ScreenshotUtils.attachToAllure("soft-assert-failure-" + failures.size());
        }

        public List<String> getFailures() {
            return new ArrayList<>(failures);
        }

        public int getFailureCount() {
            return failures.size();
        }

        public boolean hasFailures() {
            return !failures.isEmpty();
        }
    }
}

