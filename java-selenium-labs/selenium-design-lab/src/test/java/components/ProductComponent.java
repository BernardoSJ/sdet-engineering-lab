package components;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ProductComponent {

    private final WebElement root;

    private final By name = By.className("inventory_item_name");
    private final By description = By.className("inventory_item_desc");
    private final By price = By.className("inventory_item_price");
    private final By addToCartButton =  By.cssSelector("button[id^='add-to-cart']");

    public ProductComponent(WebElement root) {
        this.root = root;
    }

    public String getName() {
        return root.findElement(name).getText();
    }

    public String getDescription() {
        return root.findElement(description).getText();
    }

    public String getPrice() {
        return root.findElement(price).getText();
    }

    public void addToCart() {
        root.findElement(addToCartButton).click();
    }

}
