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
        AppiumManager.startIfLocal();
    }

    @AfterSuite(alwaysRun = true)
    public void globalTearDown() {
        AppiumManager.stopIfStarted();
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
