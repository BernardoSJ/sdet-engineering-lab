package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DynamicControlsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By checkbox = By.id("checkbox");

    private final By removeButton = By.xpath("//button[normalize-space()='Remove']");

    private final By addButton = By.xpath("//button[normalize-space()='Add']");

    public DynamicControlsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isCheckboxPresent() {
        return !driver.findElements(checkbox).isEmpty();
    }

    public void removeCheckbox() {
        WebElement currentCheckbox = wait.until(ExpectedConditions.presenceOfElementLocated(checkbox));
        wait.until(ExpectedConditions.elementToBeClickable(removeButton)).click();
        wait.until(ExpectedConditions.stalenessOf(currentCheckbox));
    }

    public void addCheckbox() {
        wait.until(ExpectedConditions.elementToBeClickable(addButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(checkbox));
    }

}
