package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SeleniumWaitsPage {

    private WebDriver driver;
    private By javaLink = By.id("tabs-6f16eaed-01-00-tab");
    private By javaScriptLink = By.id("tabs-6f16eaed-01-04-tab");
    private By kotlinLink = By.id("tabs-6f16eaed-01-05-tab");

    private By expectedConditionsDocumentationLink = By.xpath("//a[contains(text(),'Expected Conditions Documentation')]");
    private By addExampleLink = By.xpath("(//span[@data-bs-toggle='tooltip' and text()='Add Example'])");

    public SeleniumWaitsPage(WebDriver driver) {
        this.driver = driver;
    }

    public AddExamplePage clickAddExampleLinkWithinJava() {
        driver.findElement(javaLink).click();
        driver.findElements(addExampleLink).get(0).click();
        return new AddExamplePage(driver);
    }

    public boolean isExpectedConditionsDocumentationLinkDisplayed() {
        return driver.findElement(expectedConditionsDocumentationLink).isDisplayed();
    }

    public AddExamplePage clickAddExampleLinkWithinJavaScript() {
        driver.findElement(javaScriptLink).click();
        driver.findElements(addExampleLink).get(1).click();
        return new AddExamplePage(driver);
    }

    public AddExamplePage clickAddExampleLinkWithinKotlin() {
        driver.findElement(kotlinLink).click();
        driver.findElements(addExampleLink).get(2).click();
        return new AddExamplePage(driver);
    }

}
