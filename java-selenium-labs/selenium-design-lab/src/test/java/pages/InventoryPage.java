package pages;

import components.HeaderComponent;
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
    private final HeaderComponent header;

    public InventoryPage(WebDriver driver){
        this.driver = driver;
        this.header = new HeaderComponent(driver);

        List<WebElement> elements = driver.findElements(inventoryElements);
        for (WebElement element : elements) {
            products.add(new ProductComponent(element));
        }
    }

    public HeaderComponent getHeader() {
        return header;
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
        return products.stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst();
    }

}
