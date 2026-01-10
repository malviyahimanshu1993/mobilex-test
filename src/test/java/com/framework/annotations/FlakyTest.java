package com.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark flaky tests with retry configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FlakyTest {

    /**
     * Maximum number of retry attempts.
     */
    int maxRetries() default 3;

    /**
     * Delay between retries in milliseconds.
     */
    int delayMs() default 1000;

    /**
     * Reason why the test is flaky.
     */
    String reason() default "";

    /**
     * Bug/issue tracker ID if applicable.
     */
    String bugId() default "";
}

