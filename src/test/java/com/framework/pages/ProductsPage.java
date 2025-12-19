package com.framework.pages;

import com.framework.base.BasePage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ProductsPage extends BasePage {

    private static final By PRODUCTS_HEADER = AppiumBy.accessibilityId("test-PRODUCTS");

    public ProductsPage(AppiumDriver driver) {
        super(driver);
    }

    public ProductsPage waitForLoaded() {
        waitForVisible(PRODUCTS_HEADER);
        return this;
    }

    public boolean isProductDisplayed(String title) {
        List<WebElement> titles = driver.findElements(AppiumBy.accessibilityId("test-Item title"));
        for (WebElement t : titles) {
            if (title.equals(t.getText())) return true;
        }
        return false;
    }

    public void clickAddToCartForTitle(String title) {
        List<WebElement> items = driver.findElements(AppiumBy.accessibilityId("test-Item"));
        for (WebElement item : items) {
            try {
                WebElement titleElem = item.findElement(AppiumBy.accessibilityId("test-Item title"));
                if (title.equals(titleElem.getText())) {
                    WebElement addBtn = item.findElement(AppiumBy.accessibilityId("test-ADD TO CART"));
                    addBtn.click();
                    return;
                }
            } catch (Exception ignored) {
                // ignore and continue searching
            }
        }
        throw new RuntimeException("Product not found: " + title);
    }

    public void clickFirstAddToCart() {
        List<WebElement> addBtns = driver.findElements(AppiumBy.accessibilityId("test-ADD TO CART"));
        if (addBtns.isEmpty()) throw new RuntimeException("No ADD TO CART buttons found");
        addBtns.get(0).click();
    }

    public void openCart() {
        driver.findElement(AppiumBy.accessibilityId("test-Cart")).click();
    }

    public String getPriceForTitle(String title) {
        List<WebElement> items = driver.findElements(AppiumBy.accessibilityId("test-Item"));
        for (WebElement item : items) {
            try {
                WebElement titleElem = item.findElement(AppiumBy.accessibilityId("test-Item title"));
                if (title.equals(titleElem.getText())) {
                    WebElement price = item.findElement(AppiumBy.accessibilityId("test-Price"));
                    return price.getText();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
