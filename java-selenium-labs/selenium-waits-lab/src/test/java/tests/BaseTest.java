package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.SeleniumWaitsPage;

public class BaseTest {

    private static WebDriver driver;
    protected SeleniumWaitsPage seleniumWaitsPage;

    @BeforeMethod
    public void setUp() {

        driver = new ChromeDriver();;
        seleniumWaitsPage = new SeleniumWaitsPage(driver);
        driver.get("https://www.selenium.dev/documentation/webdriver/support_features/expected_conditions/");
        driver.manage().window().maximize();


    }

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }

}
