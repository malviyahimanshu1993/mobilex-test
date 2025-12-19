package com.framework.tests;

import com.framework.annotations.TestInfo;
import com.framework.base.BaseTest;
import com.framework.config.Config;
import com.framework.pages.LoginPage;
import com.framework.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInfo(author = "Himanshu", priority = "High", component = "Products")
public class ProductsTest extends BaseTest {

    @Test
    public void testAddProductToCart() {
        new LoginPage(getDriver())
                .enterUsername(Config.get().username())
                .enterPassword(Config.get().password())
                .clickLogin();

        ProductsPage products = new ProductsPage(getDriver()).waitForLoaded();

        String productTitle = "Sauce Labs Backpack";
        Assert.assertTrue(products.isProductDisplayed(productTitle), "Expected product to be visible: " + productTitle);
        Assert.assertNotNull(products.getPriceForTitle(productTitle), "Price should be present for product: " + productTitle);

        products.clickAddToCartForTitle(productTitle);
        products.openCart();
    }
}
