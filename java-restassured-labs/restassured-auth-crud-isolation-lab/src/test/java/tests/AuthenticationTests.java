package tests;


import clients.AuthClient;
import config.ApiSpecifications;
import io.restassured.response.Response;
import models.AuthPayload;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AuthenticationTests {

    AuthClient authClient;
    ApiSpecifications apiSpecifications;

    @BeforeMethod
    public void setUpRequestAndResponse(){
        apiSpecifications = new ApiSpecifications();
        authClient = new AuthClient(apiSpecifications.setUpRequest());
    }

    @Test(description = "Verify valid authentication")
    public void validAuthentication(){
        AuthPayload authPayload = new AuthPayload("admin", "password123");
        String token = authClient.authenticate(authPayload)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response().jsonPath().getString("token");

        Assert.assertNotNull(token,  "Authentication token is missing");
        Assert.assertFalse(token.isBlank(), "Authentication token is blank");
    }

    @Test(description = "Verify invalid authentication")
    public void invalidAuthentication(){
        AuthPayload authPayload = new AuthPayload("admin", "password12");

        Response response = authClient.authenticate(authPayload);

        response.then()
                .spec(apiSpecifications.successfulJsonResponse());

        String reason = response.jsonPath().getString("reason");
        String token = response.jsonPath().getString("token");

        Assert.assertEquals(reason, "Bad credentials", "Unexpected authentication error");
        Assert.assertNull(token, "Token should not be returned for invalid credentials");
    }

}
