package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {

    private By usernameField = By.id("user-name");
    private By passwordField = By.id("password");
    private By loginButton = By.id("login-button");
    private WebDriver driver;

    public LoginPage(WebDriver driver){
        this.driver = driver;
        this.driver.get("https://www.saucedemo.com/");
    }


    public InventoryPage logIn(String username, String password){
        driver.findElement(usernameField).sendKeys(username);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();
    }

}
