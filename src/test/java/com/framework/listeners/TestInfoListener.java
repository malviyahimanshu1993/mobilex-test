package com.framework.listeners;

import com.framework.annotations.TestInfo;
import io.qameta.allure.Allure;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.lang.reflect.Method;

public class TestInfoListener extends TestListenerAdapter {

    @Override
    public void onTestStart(ITestResult result) {
        TestInfo info = resolveTestInfo(result);
        if (info == null) return;

        if (!info.author().isBlank()) {
            Allure.label("author", info.author());
        }
        if (!info.priority().isBlank()) {
            Allure.label("priority", info.priority());
        }
        if (!info.component().isBlank()) {
            Allure.label("component", info.component());
        }
    }

    private TestInfo resolveTestInfo(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        if (method != null && method.isAnnotationPresent(TestInfo.class)) {
            return method.getAnnotation(TestInfo.class);
        }

        Class<?> clazz = result.getTestClass().getRealClass();
        if (clazz != null && clazz.isAnnotationPresent(TestInfo.class)) {
            return clazz.getAnnotation(TestInfo.class);
        }
        return null;
    }
}
