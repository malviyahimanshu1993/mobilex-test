package com.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark tests that require specific features or capabilities.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresFeature {

    /**
     * Features required for this test.
     */
    Feature[] value();

    /**
     * Whether to skip the test if feature is not available (true) or fail it (false).
     */
    boolean skipIfUnavailable() default true;

    enum Feature {
        WIFI,
        MOBILE_DATA,
        CAMERA,
        GPS,
        BLUETOOTH,
        NFC,
        BIOMETRIC,
        NOTIFICATIONS,
        CONTACTS,
        STORAGE,
        REAL_DEVICE,
        EMULATOR,
        ANDROID,
        IOS,
        LANDSCAPE_MODE,
        DARK_MODE
    }
}

