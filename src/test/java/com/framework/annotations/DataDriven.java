package com.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define data provider for a test method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataDriven {

    /**
     * Path to the data file (JSON, CSV, or Excel).
     */
    String dataFile();

    /**
     * Sheet name for Excel files.
     */
    String sheetName() default "";

    /**
     * Data filter expression.
     */
    String filter() default "";

    /**
     * Whether the path is relative to classpath or absolute.
     */
    boolean classpath() default true;
}

