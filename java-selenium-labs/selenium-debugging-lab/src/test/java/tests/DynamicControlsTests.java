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

}
