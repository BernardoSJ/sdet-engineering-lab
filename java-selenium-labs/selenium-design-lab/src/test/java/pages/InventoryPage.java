package pages;

import components.ProductComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryPage {

    private final WebDriver driver;
    private final By productsLabel = By.xpath("//span[@class='title']");
    private final List<ProductComponent> products = new ArrayList<>();
    private final By inventoryElements = By.xpath("//div[@data-test='inventory-item']");

    public InventoryPage(WebDriver driver){
        this.driver = driver;
        List<WebElement> elements = driver.findElements(inventoryElements);
        for (WebElement element : elements) {
            products.add(new ProductComponent(element));
        }
    }

    public int getNumberOfProducts(){
        return products.size();
    }


    public boolean isUserLogged(){
        return driver.findElement(productsLabel).isDisplayed();
    }

    public List<ProductComponent> getProducts() {
        return List.copyOf(products);
    }

    public Optional<ProductComponent> getProductByName(String productName){
        for (ProductComponent product : products) {
            if (product.getName().equals(productName)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

}
