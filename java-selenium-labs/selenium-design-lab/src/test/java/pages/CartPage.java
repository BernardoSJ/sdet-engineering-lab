package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {

    private final By inventoryItem = By.className("inventory_item_name");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean containsProduct(String productName) {
        return driver.findElements(inventoryItem)
                .stream()
                .anyMatch(element ->
                        element.getText().equals(productName));
    }

}
