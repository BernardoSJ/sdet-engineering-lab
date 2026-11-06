package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class DynamicControlsTests extends BaseTest{

    @Test(description = "Validate Checkbox exists after removal")
    public void testRemoveCheckbox() {
        Assert.assertTrue(dynamicControlsPage.isCheckboxPresent(), "Checkbox should be present before removal");
        WebElement checkbox = driver.findElement(By.id("checkbox"));
        dynamicControlsPage.removeCheckbox();

        WebDriverWait wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(10)
        );

        wait.until(ExpectedConditions.stalenessOf(checkbox));

        checkbox.isDisplayed();
    }

    @Test(description = "Validate CheckBox does not exist after removal")
    public void testNoElementExceptionAfterRemovingCheckbox() {
        Assert.assertTrue(dynamicControlsPage.isCheckboxPresent(), "Checkbox should be present before removal");
        WebElement checkbox = driver.findElement(By.id("checkbox"));
        dynamicControlsPage.removeCheckbox();

        WebDriverWait wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(10)
        );
        wait.until(ExpectedConditions.stalenessOf(checkbox));

        WebElement checkboxAfterRemoval = driver.findElement(By.id("checkbox"));

        checkboxAfterRemoval.isDisplayed();
    }

    @Test(description = "Validate timeout when checkbox does not reappear")
    public void testTimeOutExceptionAfterRemovingCheckbox() {

        Assert.assertTrue(dynamicControlsPage.isCheckboxPresent(),  "Checkbox should be present before removal");

        WebElement checkbox = driver.findElement(By.id("checkbox"));

        dynamicControlsPage.removeCheckbox();

        WebDriverWait removalWait = new WebDriverWait(driver, Duration.ofSeconds(10));

        removalWait.until(ExpectedConditions.stalenessOf(checkbox));

        WebDriverWait appearanceWait = new WebDriverWait(driver, Duration.ofSeconds(3));

        appearanceWait.until(ExpectedConditions.presenceOfElementLocated(By.id("checkbox")));
    }

    @Test(description = "Validate Checkbox exists after adding")
    public void validateCheckIsPresentAfterAdd(){
        Assert.assertTrue(dynamicControlsPage.isCheckboxPresent(), "Checkbox should initially exist");
        dynamicControlsPage.removeCheckbox();
        Assert.assertFalse(dynamicControlsPage.isCheckboxPresent(), "Checkbox should not exist after removal");
        dynamicControlsPage.addCheckbox();
        Assert.assertTrue(dynamicControlsPage.isCheckboxPresent(), "Checkbox should exist after addition");
    }

}
