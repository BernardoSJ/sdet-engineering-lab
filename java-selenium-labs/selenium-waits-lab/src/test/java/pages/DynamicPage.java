package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DynamicPage {

    private WebDriver driver;
    private By addBoxButton = By.id("adder");
    private By addedBox = By.id("box0");

    private By revealInputButton = By.id("reveal");
    private By revealedInput = By.id("revealed");

    public DynamicPage(WebDriver driver){
        this.driver = driver;
    }

    public DynamicPage clickAddBoxButton(){
        driver.findElement(addBoxButton).click();
        return this;
    }

    public boolean isAddedBoxDisplayed() throws InterruptedException {
        Thread.sleep(2000);
        return driver.findElement(addedBox).isDisplayed();
    }

    public DynamicPage clickRevealInputButton(){
        driver.findElement(revealInputButton).click();
        return this;
    }

    public boolean isRevealedInputDisplayed() throws InterruptedException {
        Thread.sleep(2000);
        return driver.findElement(revealedInput).isDisplayed();
    }

}
