package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DynamicPage {

    private WebDriver driver;

    private By addBoxButton = By.id("adder");
    private By addedBox = By.id("box0");

    private By revealInputButton = By.id("reveal");
    private By revealedInput = By.id("revealed");

    private WebDriverWait wait;

    public DynamicPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(2)
        );
    }

    public DynamicPage clickAddBoxButton() {
        driver.findElement(addBoxButton).click();
        return this;
    }

    public boolean isAddedBoxPresent() {
        return wait.until(
                ExpectedConditions
                        .presenceOfElementLocated(addedBox)
        ) != null;
    }

    public DynamicPage clickRevealInputButton() {
        driver.findElement(revealInputButton).click();
        return this;
    }

    public boolean isRevealedInputVisible() {
        return wait.until(
                ExpectedConditions
                        .visibilityOfElementLocated(revealedInput)
        ) != null;
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
