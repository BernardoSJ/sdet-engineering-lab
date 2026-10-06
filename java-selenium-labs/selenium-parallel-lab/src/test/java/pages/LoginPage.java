package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.latest.log.Log;

public class LoginPage {

    private WebDriver driver;
    private By usernameField = By.id("user-name");
    private By passwordField = By.id("password");
    private By loginButton = By.id("login-button");
    private By errorContainerMessage = By.xpath("//h3[@data-test='error']");
    private By logo = By.className("login_logo");

    public LoginPage(WebDriver driver){
        this.driver = driver;
    }

    public InventoryPage logInWithValidCredentials(String username, String password){
        driver.findElement(usernameField).sendKeys(username);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();
        InventoryPage inventoryPage = new InventoryPage(driver);
        return inventoryPage;
    }

    public LoginPage logInWithIncorrectCredentials(String username, String password){
        driver.findElement(usernameField).sendKeys(username);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();
        LoginPage loginPage = new LoginPage(driver);
        return loginPage;
    }

    public String getErrorMessage(){
        return driver.findElement(errorContainerMessage).getText();
    }

    public boolean isLogoVisible(){
        return driver.findElement(logo).isDisplayed();
    }

}
