package com.framework.pages;

import com.framework.base.BasePage;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

public class LoginPage extends BasePage {

    @AndroidFindBy(accessibility = "test-Username")
    private WebElement usernameField;

    @AndroidFindBy(accessibility = "test-Password")
    private WebElement passwordField;

    @AndroidFindBy(accessibility = "test-LOGIN")
    private WebElement loginButton;

    @AndroidFindBy(xpath = "//*[contains(@text, 'locked out')]")
    private WebElement errorMessage;

    public LoginPage(AppiumDriver driver) {
        super(driver);
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(5)), this);
    }

    public LoginPage enterUsername(String user) {
        usernameField.sendKeys(user);
        return this;
    }

    public LoginPage enterPassword(String pass) {
        passwordField.sendKeys(pass);
        return this;
    }

    public LoginPage clickLogin() {
        loginButton.click();
        return this;
    }

    public void verifyErrorMessage(String s) {
        // Implementation for verifying error message
        String actualMessage = errorMessage.getText();
        System.out.println("page source: " +driver.getPageSource());
        if (!actualMessage.equals(s)) {
            throw new AssertionError("Expected error message: " + s + " but got: " + actualMessage);
        }
    }
}
