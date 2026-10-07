package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.time.Duration;

public class InventoryPage {

    private WebDriver driver;
    private By productsLabel = By.xpath("//span[@class='title']");
    private By inventoryElements = By.xpath("//div[@data-test='inventory-item-name']");
    private By optionsButton = By.id("react-burger-menu-btn");
    private String logoutButtonXpath = "//a[@data-test='logout-sidebar-link']";

    public InventoryPage(WebDriver driver){
        this.driver = driver;
    }

    public int getNumberOfProducts(){
        return driver.findElements(inventoryElements).size();
    }

    public LoginPage logOut(){
        driver.findElement(optionsButton).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu-wrap")));
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(logoutButtonXpath)));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", logoutBtn);
        LoginPage loginPage = new LoginPage(driver);
        return loginPage;
    }

    public boolean isUserLogged(){
        return driver.findElement(productsLabel).isDisplayed();
    }

}
