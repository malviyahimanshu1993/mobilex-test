package com.framework.tests;

import com.framework.annotations.TestInfo;
import com.framework.base.BaseTest;
import com.framework.config.Config;
import com.framework.pages.LoginPage;
import org.testng.annotations.Test;

@TestInfo(author = "Himanshu", priority = "Smoke", component = "Login")
public class LoginTest extends BaseTest {

    @Test
    public void testLoginButtonClickable() {
        new LoginPage(getDriver())
                .enterUsername(Config.get().username())
                .enterPassword(Config.get().password())
                .clickLogin();
    }

    @Test
    public void testLockedOutUserCannotLogin() {
        new LoginPage(getDriver())
                .enterUsername("locked_out_user")
                .enterPassword(Config.get().password())
                .clickLogin()
                .verifyErrorMessage("Sorry, this user has been locked out.");
    }


}
