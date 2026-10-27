package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import utils.WaitUtility;

import java.time.Duration;

public class DynamicPage {

    private final WebDriver driver;
    private final WaitUtility waitUtiliy;

    private By addBoxButton = By.id("adder");
    private By addedBox = By.id("box0");

    private By revealInputButton = By.id("reveal");
    private By revealedInput = By.id("revealed");


    public DynamicPage(WebDriver driver) {
        this.driver = driver;
        waitUtiliy = new WaitUtility(driver);
    }

    public DynamicPage clickAddBoxButton() {
        driver.findElement(addBoxButton).click();
        return this;
    }

    public boolean isAddedBoxPresent() {
        return waitUtiliy.waitForPresence(addedBox) != null;
    }

    public DynamicPage clickRevealInputButton() {
        driver.findElement(revealInputButton).click();
        return this;
    }

    public boolean isRevealedInputVisible() {
        return waitUtiliy.waitForVisibility(revealedInput) != null;
    }

    public boolean isAddedBoxPresentWithFluentWait() {

        Wait<WebDriver> fluentWait =
                new FluentWait<>(driver)
                        .withTimeout(Duration.ofSeconds(5))
                        .pollingEvery(Duration.ofMillis(200))
                        .ignoring(NoSuchElementException.class);

        return fluentWait.until(d -> {

            System.out.println(
                    "Checking box at: "
                            + System.currentTimeMillis()
            );

            return d.findElement(addedBox).isDisplayed();
        });
    }

}
