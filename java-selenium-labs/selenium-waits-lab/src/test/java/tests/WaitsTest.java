package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WaitsTest extends BaseTest{

    @Test(description = "Expected Conditions Documentation link is displayed")
    public void expectedConditionsValid(){
        boolean isLinkVisible = seleniumWaitsPage.isExpectedConditionsDocumentationLinkDisplayed();
        Assert.assertTrue(isLinkVisible);
    }

    @Test(description = "Add Example Link Within Java Works")
    public void addExampleLinkWithJavaWorks(){
        boolean isLinkVisible = seleniumWaitsPage.clickAddExampleLinkWithinJava().isAddExampleLinkDisplayed();
        Assert.assertTrue(isLinkVisible);
    }

    @Test(description = "Add Example Link Within JavaScript Works")
    public void addExampleLinkWithJavaScriptWorks(){
        boolean isLinkVisible = seleniumWaitsPage.clickAddExampleLinkWithinJavaScript().isAddExampleLinkDisplayed();
        Assert.assertTrue(isLinkVisible);
    }

    @Test(description = "Add Example Link Within JavaScript Works")
    public void addExampleLinkWithKotlinWorks(){
        boolean isLinkVisible = seleniumWaitsPage.clickAddExampleLinkWithinKotlin().isAddExampleLinkDisplayed();
        Assert.assertTrue(isLinkVisible);
    }

}
