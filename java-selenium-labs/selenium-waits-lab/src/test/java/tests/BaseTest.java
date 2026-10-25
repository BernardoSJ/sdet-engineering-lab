package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.DynamicPage;

import java.time.Duration;

public class BaseTest {

    protected WebDriver driver;
    protected DynamicPage dynamicPage;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        /*driver.manage()
                .timeouts()
                .implicitlyWait(Duration.ofSeconds(10));*/
        driver.get("https://www.selenium.dev/selenium/web/dynamic.html");
        dynamicPage = new DynamicPage(driver);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
