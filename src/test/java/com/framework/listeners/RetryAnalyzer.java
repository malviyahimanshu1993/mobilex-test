package com.framework.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private final int maxRetries;

    public RetryAnalyzer() {
        this.maxRetries = Integer.parseInt(System.getProperty("retry.count", "1"));
    }

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetries) {
            retryCount++;
            return true;
        }
        return false;
    }
}

