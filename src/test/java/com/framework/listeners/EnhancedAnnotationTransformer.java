package com.framework.listeners;

import com.framework.annotations.DataDriven;
import com.framework.utils.DataUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Annotation transformer that enhances test annotations with additional functionality.
 */
public class EnhancedAnnotationTransformer implements IAnnotationTransformer {

    private static final Logger log = LogManager.getLogger(EnhancedAnnotationTransformer.class);

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {

        // Apply enhanced retry analyzer if none is set
        if (annotation.getRetryAnalyzerClass() == null) {
            annotation.setRetryAnalyzer(EnhancedRetryAnalyzer.class);
        }

        // Check for @DataDriven annotation
        if (testMethod != null) {
            DataDriven dataDriven = testMethod.getAnnotation(DataDriven.class);
            if (dataDriven != null) {
                // Set data provider
                annotation.setDataProviderClass(DynamicDataProvider.class);
                annotation.setDataProvider("csvData");
                log.debug("Applied data provider for test: {}", testMethod.getName());
            }
        }
    }

    @Override
    public void transform(IDataProviderAnnotation annotation, Method method) {
        // Can be used to modify data provider behavior
    }
}

