package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AddExamplePage {

    private WebDriver driver;
    private By addExampleLink = By.xpath("(//span[@data-bs-toggle='tooltip' and text()='Add Example'])");

    public AddExamplePage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isAddExampleLinkDisplayed() {
        return driver.findElement(addExampleLink).isDisplayed();
    }

}
