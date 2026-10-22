package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WaitsTest extends BaseTest{

    @Test(description = "New Box Appears After Clicking Add Box Button")
    public void expectedConditionsValid() throws InterruptedException {
        long start = System.currentTimeMillis();
        boolean isNewBoxVisible = dynamicPage.clickAddBoxButton().isAddedBoxDisplayed();
        Assert.assertTrue(isNewBoxVisible);
        long end = System.currentTimeMillis();
        System.out.println("Duration: " + (end - start) + " ms");
    }

    @Test(description = "New Input Appears After Clicking Reveal Input Button")
    public void addExampleLinkWithJavaWorks() throws InterruptedException {
        long start = System.currentTimeMillis();
        boolean isNewInputVisible = dynamicPage.clickRevealInputButton().isRevealedInputDisplayed();
        Assert.assertTrue(isNewInputVisible);
        long end = System.currentTimeMillis();
        System.out.println("Duration: " + (end - start) + " ms");
    }


}
