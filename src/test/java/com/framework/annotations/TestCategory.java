package com.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify test category and grouping.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TestCategory {

    /**
     * Test type (e.g., smoke, regression, sanity).
     */
    TestType type() default TestType.REGRESSION;

    /**
     * Test severity.
     */
    Severity severity() default Severity.NORMAL;

    /**
     * Feature area being tested.
     */
    String feature() default "";

    /**
     * Story or requirement ID.
     */
    String story() default "";

    /**
     * Epic ID.
     */
    String epic() default "";

    /**
     * Custom tags for filtering.
     */
    String[] tags() default {};

    enum TestType {
        SMOKE,
        SANITY,
        REGRESSION,
        INTEGRATION,
        E2E,
        PERFORMANCE,
        SECURITY,
        ACCESSIBILITY,
        NEGATIVE,
        EXPLORATORY
    }

    enum Severity {
        BLOCKER,
        CRITICAL,
        NORMAL,
        MINOR,
        TRIVIAL
    }
}

