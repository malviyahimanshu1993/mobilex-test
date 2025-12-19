package com.framework.listeners;

import com.framework.base.AppiumManager;
import org.testng.IExecutionListener;

public class AppiumSuiteListener implements IExecutionListener {

    @Override
    public void onExecutionStart() {
        AppiumManager.startIfLocal();
    }

    @Override
    public void onExecutionFinish() {
        AppiumManager.stopIfStarted();
    }
}

