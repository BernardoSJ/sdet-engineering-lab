package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DynamicControlsPage {

    private final WebDriver driver;

    private final By addRemoveButton = By.xpath("//button[text()='Remove' or text()='Add']");
    private final By enableDisableButton = By.xpath("//button[text()='Enable' or text()='Disable']");
    private final By checkbox = By.id("checkbox");
    private final By messageLabel = By.id("message");

    private final By loadingCheckbox = By.xpath("(//*[@id='loading'])[1]");
    private final By loadingInput = By.xpath("(//*[@id='loading'])[2]");
    private final By inputField = By.xpath("//input[@type='text']");

    public DynamicControlsPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isCheckboxPresent(){
        return !driver.findElements(checkbox).isEmpty();
    }

    public void removeCheckbox(){
        if(driver.findElement(addRemoveButton).getText().equals("Remove")){
            driver.findElement(addRemoveButton).click();
        }
    }

    public void addCheckbox(){
        if(driver.findElement(addRemoveButton).getText().equals("Add")){
            driver.findElement(addRemoveButton).click();
        }
    }

    public boolean isInputEnabled(){
        return driver.findElement(inputField).isEnabled();
    }

    public void enableInput(){
        if(driver.findElement(enableDisableButton).getText().equals("Enable")){
            driver.findElement(enableDisableButton).click();
        }
    }

    public void disableInput(){
        if(driver.findElement(enableDisableButton).getText().equals("Disable")){
            driver.findElement(enableDisableButton).click();
        }
    }

    public String getMessageText(){
        return driver.findElement(messageLabel).getText();
    }

    public boolean isLoadingCheckboxDisplayed(){
        return driver.findElement(loadingCheckbox).isDisplayed();
    }

    public boolean isLoadingInputDisplayed(){
        return driver.findElement(loadingInput).isDisplayed();
    }

}
