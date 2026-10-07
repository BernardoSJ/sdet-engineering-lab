package tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.LoginPage;

import java.lang.reflect.Method;

public class BaseTest {

    protected static WebDriver driver;
    protected LoginPage loginPage;

    @BeforeMethod
    public void setUp(Method method) {
        long threadId = Thread.currentThread().threadId();
        System.out.println(
                "[SETUP START] Test: " + method.getName()
                        + " | Thread: " + threadId
        );

        driver = new ChromeDriver();

        System.out.println(
                "[DRIVER CREATED] Test: " + method.getName()
                        + " | Thread: " + threadId
                        + " | Driver Identity: "
                        + System.identityHashCode(driver)
        );

        driver.get("https://www.saucedemo.com/");
        driver.manage().window().maximize();
        loginPage = new LoginPage(driver);

        System.out.println(
                "[PAGE CREATED] Test: " + method.getName()
                        + " | Thread: " + threadId
                        + " | LoginPage Identity: "
                        + System.identityHashCode(loginPage)
        );
    }

    @AfterMethod
    public void tearDown(Method method) {
        long threadId = Thread.currentThread().threadId();

        System.out.println(
                "[TEARDOWN] Test: " + method.getName()
                        + " | Thread: " + threadId
                        + " | Driver Identity: "
                        + System.identityHashCode(driver)
        );

        driver.quit();
    }

}
