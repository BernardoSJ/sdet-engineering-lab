package tests.experiments;

import clients.AuthClient;
import clients.BookingClient;
import config.ApiSpecifications;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.AuthPayload;
import models.BookingCreatedResponse;
import models.BookingDates;
import models.BookingRequest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class CleanupFailureHandlingDemo {

    AuthClient authClient;
    BookingClient bookingClient;
    ApiSpecifications apiSpecifications;

    private int bookingId;
    private String token;

    @BeforeMethod
    public void setUpRequestAndResponse(){
        bookingId = 0;
        token = "";
        apiSpecifications = new ApiSpecifications();
        RequestSpecification requestSpecification = apiSpecifications.setUpRequest();
        bookingClient = new BookingClient(requestSpecification);
        authClient = new AuthClient(requestSpecification);
    }

    private void authenticateUser() {
        AuthPayload authPayload = new AuthPayload("admin", "password123");

        token = authClient.authenticate(authPayload)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract()
                .jsonPath()
                .getString("token");

        Assert.assertNotNull(token, "Authentication token is missing");
        Assert.assertFalse(token.isBlank(), "Authentication token is blank");
    }

    @Test(description = "Test Isolation Demo")
    public void testIsolationDemo(){
        int createdBookingId = 0;
        Throwable primaryFailure = null;
        try{
            BookingDates initialDates = new BookingDates("2026-12-10", "2026-12-15");

            BookingRequest initialBookingRequest = new BookingRequest("Alex",
                    "Smith", 250, true, initialDates, "Breakfast");
            Map<String, Object> partialPayload = new HashMap<>();
            partialPayload.put("firstname", "Michael");
            partialPayload.put("totalprice", 350);


            authenticateUser();

            Response bookingCreated = bookingClient.createBooking(initialBookingRequest)
                    .then()
                    .spec(apiSpecifications.successfulJsonResponse())
                    .extract().response();

            BookingCreatedResponse bookingCreatedResponse = bookingCreated.as(BookingCreatedResponse.class);
            createdBookingId = bookingCreatedResponse.getBookingid();
            Assert.assertTrue(createdBookingId > 0, "Invalid booking ID returned after creation");
            bookingId = createdBookingId;
            Response partiallyUpdatedBooking = bookingClient.partiallyUpdateBooking(createdBookingId, partialPayload, token)
                    .then()
                    .spec(apiSpecifications.successfulJsonResponse())
                    .extract().response();

            BookingRequest partiallyUpdatedObject = partiallyUpdatedBooking.as(BookingRequest.class);
            Assert.assertEquals(partiallyUpdatedObject.getFirstname(), partialPayload.get("firstname"));
            Assert.assertEquals(partiallyUpdatedObject.getTotalprice(), partialPayload.get("totalprice"), "Total Price is not correct");


        }catch(Throwable failure) {
            primaryFailure = failure;
            throw failure;
        }finally{

            try{
                Response deleteResponse = bookingClient.deleteBooking(createdBookingId, token);
                Assert.assertEquals(deleteResponse.statusCode(), 201);
                Assert.assertEquals(deleteResponse.body().asString(), "Created");

                Response retrievedBooking = bookingClient.getBookingById(createdBookingId)
                        .then()
                        .extract().response();
                Assert.assertEquals(retrievedBooking.statusCode(), 404);
                bookingId = 0;
            }catch(Throwable cleanupFailure){
                bookingId = 0;
                if (primaryFailure != null) {
                    primaryFailure.addSuppressed(cleanupFailure);
                } else {
                    throw cleanupFailure;
                }
            }
        }


    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp(){
        if (bookingId <= 0) {
            return;
        }

        Assert.assertNotNull(token, "Cleanup token is missing");
        Assert.assertFalse(token.isBlank(), "Cleanup token is blank");

        Response deleteResponse = bookingClient.deleteBooking(bookingId, token);
        Assert.assertEquals(deleteResponse.statusCode(), 201, "Cleanup failed for booking ID: " + bookingId);

        Response retrievedBooking = bookingClient.getBookingById(bookingId);

        Assert.assertEquals(retrievedBooking.statusCode(), 404, "Booking still accessible after cleanup: " + bookingId
        );
        System.out.println("[CLEANUP] Booking " + bookingId + " deleted");
        bookingId = 0;
    }
}
