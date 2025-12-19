package com.framework.listeners;

import com.framework.base.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class AllureTestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            var driver = DriverManager.getDriver();
            if (driver != null) {
                try {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    Allure.addAttachment("screenshot-" + result.getName(), new ByteArrayInputStream(screenshot));
                } catch (ClassCastException ignored) {
                    // driver doesn't support screenshots - ignore
                }
            }
        } catch (Exception ignored) {
            // continue - best effort
        }

        if (result.getThrowable() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                result.getThrowable().printStackTrace(pw);
                Allure.addAttachment("exception-" + result.getName(), "text/plain", sw.toString(), ".log");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // no-op
    }

    @Override
    public void onStart(ITestContext context) {
        // no-op
    }

    @Override
    public void onFinish(ITestContext context) {
        // no-op
    }
}
