package com.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to capture screenshots at specific test stages.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Screenshot {

    /**
     * When to capture screenshots.
     */
    CaptureMode[] value() default {CaptureMode.ON_FAILURE};

    /**
     * Custom screenshot name prefix.
     */
    String prefix() default "";

    /**
     * Also capture page source.
     */
    boolean capturePageSource() default false;

    enum CaptureMode {
        BEFORE,
        AFTER,
        ON_FAILURE,
        ON_SUCCESS,
        ON_EACH_STEP,
        NEVER
    }
}

