# Appium Test Automation Framework Specifications (Strict Mode)

**Role:** Senior SDET
**Goal:** Generate a production-grade, hybrid mobile automation framework for Android and iOS using Appium 2.0 standards.

## ⛔ CRITICAL "DO NOT" RULES (Must Follow)
1.  **NO Custom Library Packages:**
    * **NEVER** create classes inside the package `io.appium.java_client` or `org.openqa.selenium`.
    * **NEVER** attempt to "implement" `AndroidDriver` or `IOSDriver` manually. You must strictly **IMPORT** them from the Maven dependencies.
2.  **NO Generics on Drivers:**
    * **STRICTLY FORBIDDEN:** `AndroidDriver<WebElement>`, `IOSDriver<MobileElement>`. (This is outdated Appium 7 syntax).
    * **CORRECT SYNTAX:** `AndroidDriver driver;` or `IOSDriver driver;`.
3.  **NO Deprecated Classes:**
    * Do NOT use: `TouchAction`, `MobileElement`, `DesiredCapabilities`.
    * USE instead: `Sequence` (W3C Actions), `WebElement`, `UiAutomator2Options`/`XCUITestOptions`.

## 1. Technology Stack
* **Build Tool:** Maven
* **Language:** Java 17+
* **Appium Client:** `io.appium:java-client:9.x.x` (Latest)
* **Test Runner:** TestNG
* **Reporting:** Allure

## 2. Implementation Guidelines

### A. Driver Initialization (The Only Valid Approach)
Use `ThreadLocal` for parallel execution. Initialize drivers using Options classes only.

**Correct Imports Only:**
```java
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
```

Android Logic:

Use UiAutomator2Options.

Set automationName to AutomationName.ANDROID_UIAUTOMATOR2.

iOS Logic (Placeholder):

Use XCUITestOptions.

Set automationName to AutomationName.IOS_XC_TEST.

Use a dummy bundleId (e.g., "com.example.ios.app") since we are just setting up the structure.

B. Page Object Model (Hybrid)
Use the Page Factory Pattern with AppiumFieldDecorator.

Every Page Class must define locators for both platforms using annotations.

Example Page Structure:

Java

public class LoginPage {
@AndroidFindBy(accessibility = "login_btn")
@iOSXCUITFindBy(accessibility = "login_button_ios_placeholder")
private WebElement loginButton;

    public LoginPage(AppiumDriver driver) {
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }
}
C. Utilities
Gestures: Create a GestureUtils class. Implement scrolling and swiping using the W3C Actions API (org.openqa.selenium.interactions.Sequence). Do NOT use TouchAction.

3. Directory Structure
   Generate code only for this structure:

Plaintext

src/test/java
├── com.framework
│   ├── base
│   │   ├── BaseTest.java          // @BeforeMethod/@AfterMethod
│   │   └── DriverManager.java     // ThreadLocal logic
│   ├── pages
│   │   └── LoginPage.java         // Hybrid locators
│   ├── utils
│   │   └── GestureUtils.java      // W3C Actions
│   └── tests
│       └── LoginTest.java