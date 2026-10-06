package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.latest.log.Log;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class InventoryPage {

    private static final Logger log = LoggerFactory.getLogger(InventoryPage.class);
    private WebDriver driver;
    private By productsLabel = By.xpath("//span[@class='title']");
    private By inventoryElements = By.xpath("//div[@data-test='inventory-item-name']");
    private By optionsButton = By.id("react-burger-menu-btn");
    private String logoutButtonId = "logout_sidebar_link";

    public InventoryPage(WebDriver driver){
        this.driver = driver;
    }

    public int getNumberOfProducts(){
        return driver.findElements(inventoryElements).size();
    }

    public LoginPage logOut(){
        driver.findElement(optionsButton).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(logoutButtonId)));
        driver.findElement(By.id(logoutButtonId)).click();
        LoginPage loginPage = new LoginPage(driver);
        return loginPage;
    }

    public boolean isUserLogged(){
        return driver.findElement(productsLabel).isDisplayed();
    }

}
