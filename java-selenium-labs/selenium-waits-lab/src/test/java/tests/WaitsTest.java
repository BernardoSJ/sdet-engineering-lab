package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WaitsTest extends BaseTest{

    @Test(description = "New Box Appears After Clicking Add Box Button")
    public void newBoxPresent(){
        long start = System.currentTimeMillis();
        boolean isNewBoxVisible = dynamicPage.clickAddBoxButton().isAddedBoxPresent();
        Assert.assertTrue(isNewBoxVisible);
        long end = System.currentTimeMillis();
        System.out.println("Duration: " + (end - start) + " ms");
    }

    @Test(description = "New Input Appears After Clicking Reveal Input Button")
    public void newInputVisible(){
        long start = System.currentTimeMillis();
        boolean isNewInputVisible = dynamicPage.clickRevealInputButton().isRevealedInputVisible();
        Assert.assertTrue(isNewInputVisible);
        long end = System.currentTimeMillis();
        System.out.println("Duration: " + (end - start) + " ms");
    }

    @Test(description = "New Box Appears Using FluentWait")
    public void newBoxPresentWithFluentWait() {
        long start = System.currentTimeMillis();
        boolean isBoxPresent = dynamicPage.clickAddBoxButton()
                        .isAddedBoxPresentWithFluentWait();
        Assert.assertTrue(isBoxPresent);
        long end = System.currentTimeMillis();
        System.out.println("FluentWait Duration: " + (end - start) + " ms");
    }

}
