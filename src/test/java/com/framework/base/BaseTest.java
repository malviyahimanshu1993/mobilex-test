package com.framework.base;

import com.framework.config.Config;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void globalSetUp() {
        // In Jenkins, Appium is started on the agent (host) and tests run in Docker.
        // Never attempt to start Appium programmatically from inside the tests JVM/container.
        // Appium endpoint is provided via appiumServerUrl / APPIUM_SERVER_URL.
    }

    @AfterSuite(alwaysRun = true)
    public void globalTearDown() {
        // No-op: do not manage Appium lifecycle from tests.
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        DriverManager.initDriver(Config.get().platform());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }

    protected AppiumDriver getDriver() {
        return DriverManager.getDriver();
    }

    protected AndroidDriver getAndroidDriver() {
        return DriverManager.getAndroidDriver();
    }

    protected IOSDriver getIOSDriver() {
        return DriverManager.getIOSDriver();
    }
}
