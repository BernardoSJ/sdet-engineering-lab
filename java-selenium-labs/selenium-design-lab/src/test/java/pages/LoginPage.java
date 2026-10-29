package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    private final WebDriver driver;
    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By errorContainerMessage = By.xpath("//h3[@data-test='error']");
    private final By logo = By.className("login_logo");

    public LoginPage(WebDriver driver){
        this.driver = driver;
    }

    public InventoryPage logInWithValidCredentials(String username, String password){
        driver.findElement(usernameField).sendKeys(username);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();
        return new InventoryPage(driver);
    }

    public LoginPage logInWithIncorrectCredentials(String username, String password){
        driver.findElement(usernameField).sendKeys(username);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();
        return this;
    }

    public String getErrorMessage(){
        return driver.findElement(errorContainerMessage).getText();
    }

    public boolean isLogoVisible(){
        return driver.findElement(logo).isDisplayed();
    }

}
