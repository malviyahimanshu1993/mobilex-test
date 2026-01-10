package com.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to measure and report test execution time.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PerformanceTest {

    /**
     * Maximum expected execution time in milliseconds.
     */
    long maxDurationMs() default 30000;

    /**
     * Whether to fail the test if duration exceeds max.
     */
    boolean failOnExceed() default false;

    /**
     * Whether to log performance metrics.
     */
    boolean logMetrics() default true;

    /**
     * Warmup iterations before measuring.
     */
    int warmupIterations() default 0;

    /**
     * Number of measurement iterations.
     */
    int iterations() default 1;
}

