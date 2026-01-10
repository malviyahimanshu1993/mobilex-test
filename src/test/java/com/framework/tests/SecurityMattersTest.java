package com.framework.tests;

import com.framework.annotations.TestInfo;
import com.framework.base.BaseTest;
import com.framework.pages.SecurityMattersPage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInfo(author = "Himanshu", priority = "Smoke", component = "Security")
public class SecurityMattersTest extends BaseTest {

    @Test()
    public void testSecurityMattersScreenElementsVisible() {
        SecurityMattersPage page = new SecurityMattersPage(getDriver())
                .verifyHeadingIsVisible()
                .verifyContentIsVisible();

        Assert.assertEquals(page.getHeadingText(), "Security matters", "Heading text should match");
        // We only assert that some non-empty content is present in the middle
        Assert.assertFalse(page.getContentText().isEmpty(), "Content text should not be empty");
    }

    @Test(priority = 1)
    public void testAgreeAndContinueButtonClickable() throws InterruptedException {
        SecurityMattersPage page = new SecurityMattersPage(getDriver())
                .verifyHeadingIsVisible();

        page.tapAgreeAndContinue();
        // If needed, add assertions for the next screen here once defined
        Thread.sleep(3000); // Temporary wait to observe the transition; replace with proper wait in real tests

        // perform scroll to bottom to verify we are on next screen
        // using UIAutomator scroll as an example
        getDriver().findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(1)"));
        getDriver().findElement(AppiumBy.androidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView("
                + "new UiSelector().textContains(\"Glossary\"))"));

        getDriver().findElement(AppiumBy.androidUIAutomator("new UiSelector().textContains(\"I agree to the\")")).click();
        WebElement continueBtn = getDriver().findElement(AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Continue\")"));

        if (continueBtn.isEnabled()) {
            continueBtn.click();
        }
        Thread.sleep(3000); // Temporary wait to observe the transition; replace with proper wait in real tests

//        System.out.println(getDriver().getPagew());
    }
}
