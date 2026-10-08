package driver;

import org.openqa.selenium.WebDriver;

public class DriverManager {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void setDriver(WebDriver driverInstance) {
        driver.set(driverInstance);
    }

    public static void removeDriver() {
        WebDriver currentDriver = driver.get();

        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } finally {
                driver.remove();
            }
        }
    }

}
