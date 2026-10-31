package components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import pages.CartPage;

public class HeaderComponent {

    private final WebDriver driver;

    private final By cartLink = By.className("shopping_cart_link");
    private final By cartBadge = By.className("shopping_cart_badge");

    public HeaderComponent(WebDriver driver) {
        this.driver = driver;
    }

    public CartPage openCart() {
        driver.findElement(cartLink).click();
        return new CartPage(driver);
    }

    public int getCartItemCount() {
        return Integer.parseInt(driver.findElement(cartBadge).getText());
    }

}
