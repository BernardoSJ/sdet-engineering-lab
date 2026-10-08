package tests;

import driver.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ThreadGuard;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.LoginPage;

import java.lang.reflect.Method;

public class BaseTest {

    protected LoginPage loginPage(){
        return new LoginPage(DriverManager.getDriver());
    }

    @BeforeMethod
    public void setUp(Method method) {
        long threadId = Thread.currentThread().threadId();
        System.out.println(
                "[SETUP START] Test: " + method.getName()
                        + " | Thread: " + threadId);

        WebDriver driver = ThreadGuard.protect(new ChromeDriver());;
        DriverManager.setDriver(driver);

        System.out.println(
                "[DRIVER CREATED] Test: " + method.getName()
                        + " | Thread: " + threadId
                        + " | Driver Identity: "
                        + System.identityHashCode(DriverManager.getDriver()));

        DriverManager.getDriver().get("https://www.saucedemo.com/");
        DriverManager.getDriver().manage().window().maximize();

        System.out.println(
                "[PAGE CREATED] Test: " + method.getName()
                        + " | Thread: " + threadId);
    }

    @AfterMethod
    public void tearDown(Method method) {
        long threadId = Thread.currentThread().threadId();

        System.out.println(
                "[TEARDOWN] Test: " + method.getName()
                        + " | Thread: " + threadId
                        + " | Driver Identity: "
                        + System.identityHashCode(DriverManager.getDriver())
        );

        DriverManager.removeDriver();
    }

}
