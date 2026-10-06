package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTests extends BaseTest{

    @Test(description = "validLogin")
    public void validLogin(){
        boolean isLogged = loginPage.logInWithValidCredentials("standard_user", "secret_sauce").isUserLogged();
        Assert.assertTrue(isLogged);
    }

    @Test(description = "invalidLogin")
    public void invalidLogin(){
        String invalidUserMessage = loginPage.logInWithIncorrectCredentials("standard_user", "secret_sauce1").getErrorMessage();
        Assert.assertEquals(invalidUserMessage,"Epic sadface: Username and password do not match any user in this service");
    }

    @Test(description = "lockedUserLogin")
    public void lockedUserLogin(){
        String lockedUserMessage = loginPage.logInWithIncorrectCredentials("locked_out_user", "secret_sauce").getErrorMessage();
        Assert.assertEquals(lockedUserMessage,"Epic sadface: Sorry, this user has been locked out.");
    }

    @Test(description = "verifyNumberOfProducts")
    public void verifyNumberOfProducts(){
        int numberOfProducts = loginPage.logInWithValidCredentials("standard_user", "secret_sauce")
                .getNumberOfProducts();
        Assert.assertEquals(numberOfProducts,6);
    }

    @Test(description = "verifyLogout")
    public void verifyLogout(){
        boolean loggedOut = loginPage.logInWithValidCredentials("standard_user", "secret_sauce")
                .logOut().isLogoVisible();
        Assert.assertTrue(loggedOut);
    }

}
