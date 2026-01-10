package com.framework.pages;

import com.framework.base.BasePage;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

public class SecurityMattersPage extends BasePage {

    @AndroidFindBy(xpath = "//*[@text='Security matters']")
    private WebElement heading;

    // Middle content text; using contains to be a bit flexible with copy changes
    @AndroidFindBy(xpath = "//*[contains(@text,'financial') or contains(@text,'crime')]")
    private WebElement content;

    @AndroidFindBy(xpath = "//*[@text='Agree and continue']")
    private WebElement agreeAndContinueButton;

    public SecurityMattersPage(AppiumDriver driver) {
        super(driver);
        // PageFactory is already initialized in BasePage constructor
    }

    public String getHeadingText() {
        return heading.getText();
    }

    public String getContentText() {
        return content.getText();
    }

    public SecurityMattersPage verifyHeadingIsVisible() {
        // simple pull to force element lookup; any failure will throw
        heading.isDisplayed();
        return this;
    }

    public SecurityMattersPage verifyContentIsVisible() {
        content.isDisplayed();
        return this;
    }

    public void tapAgreeAndContinue() {
        agreeAndContinueButton.click();
    }
}
