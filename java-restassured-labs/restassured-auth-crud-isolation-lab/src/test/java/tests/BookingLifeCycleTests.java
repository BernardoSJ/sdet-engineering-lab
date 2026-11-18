package tests;

import clients.AuthClient;
import clients.BookingClient;
import config.ApiSpecifications;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.AuthPayload;
import models.BookingCreatedResponse;
import models.BookingDates;
import models.BookingRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;


public class BookingLifeCycleTests {

    AuthClient authClient;
    BookingClient bookingClient;
    ApiSpecifications apiSpecifications;

    @BeforeMethod
    public void setUpRequestAndResponse(){
        apiSpecifications = new ApiSpecifications();
        RequestSpecification requestSpecification = apiSpecifications.setUpRequest();
        bookingClient = new BookingClient(requestSpecification);
        authClient = new AuthClient(requestSpecification);
    }

    @Test(description = "Get all bookings and verify the response")
    public void getAllBookings(){
        Response response = bookingClient.getAllBookings()
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();


        List<Map<String, Object>> bookings = response.jsonPath().getList("$");
        Assert.assertNotNull(bookings, "Missing bookings list");
        Assert.assertFalse(bookings.isEmpty(), "Expected at least one booking");

        for(Map<String, Object> booking: bookings){
            Assert.assertTrue(booking.get("bookingid") instanceof Number, "Missing or invalid bookingid");
        }
    }

    @Test(description = "Get a specific booking and verify the response")
    public void getSpecificBooking(){
        Response response = bookingClient.getAllBookings()
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();

        List<Map<String, Object>> bookings = response.jsonPath().getList("$");
        Assert.assertNotNull(bookings, "Missing bookings list");
        Assert.assertFalse(bookings.isEmpty(), "Expected at least one booking");

        Object id = bookings.get(0).get("bookingid");
        Assert.assertTrue(id instanceof Number, "Invalid booking ID");
        int bookingId = ((Number) id).intValue();

        Response specificBookingResponse = bookingClient.getBookingById(bookingId)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();

        JsonPath jsonPath = specificBookingResponse.jsonPath();

        Object name = jsonPath.get("firstname");
        Object lastName = jsonPath.get("lastname");
        Object totalPrice = jsonPath.get("totalprice");
        Object datesValue = jsonPath.get("bookingdates");
        Assert.assertTrue(datesValue instanceof Map<?, ?>, "Missing or invalid bookingdates object");

        Map<?, ?> bookingDates = (Map<?, ?>) datesValue;
        Object checkinValue = bookingDates.get("checkin");
        Object checkoutValue = bookingDates.get("checkout");

        Assert.assertTrue(checkinValue instanceof String, "Missing or invalid checkin");
        Assert.assertTrue(checkoutValue instanceof String, "Missing or invalid checkout");

        Assert.assertTrue(name instanceof String, "Missing or invalid firstname");
        Assert.assertTrue(lastName instanceof String, "Missing or invalid lastname");
        Assert.assertTrue(totalPrice instanceof Number, "Missing or invalid totalprice");
    }

    @Test(description = "Create a new booking")
    public void createNewBooking(){
        BookingDates bookingDates = new BookingDates("2026-12-10", "2026-12-15");
        BookingRequest bookingRequest = new BookingRequest("Alex",
                "Smith", 250, true, bookingDates, "Breakfast");

        Response response = bookingClient.createBooking(bookingRequest)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();


        BookingCreatedResponse createdBooking = response.as(BookingCreatedResponse.class);

        Assert.assertNotNull(createdBooking.getBooking(), "Created booking details are missing");

        BookingRequest actualBooking = createdBooking.getBooking();
        Assert.assertTrue(createdBooking.getBookingid() > 0, "Booking ID must be positive");
        Assert.assertEquals(actualBooking.getFirstname(), bookingRequest.getFirstname());
        Assert.assertEquals(actualBooking.getLastname(), bookingRequest.getLastname());
        Assert.assertEquals(actualBooking.getTotalprice(), bookingRequest.getTotalprice());
        Assert.assertEquals(actualBooking.isDepositpaid(), bookingRequest.isDepositpaid());
        Assert.assertEquals(actualBooking.getBookingdates().getCheckin(), bookingRequest.getBookingdates().getCheckin());
        Assert.assertEquals(actualBooking.getBookingdates().getCheckout(), bookingRequest.getBookingdates().getCheckout());
        Assert.assertEquals(actualBooking.getAdditionalneeds(), bookingRequest.getAdditionalneeds());


    }

    @Test(description = "Create and retrieve booking")
    public void createAndRetrieveBooking(){
        BookingDates bookingDates = new BookingDates("2026-12-10", "2026-12-15");
        BookingRequest bookingRequest = new BookingRequest("Alex",
                "Smith", 250, true, bookingDates, "Breakfast");

        Response bookingCreated = bookingClient.createBooking(bookingRequest)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();

        BookingCreatedResponse bookingCreatedResponse = bookingCreated.as(BookingCreatedResponse.class);
        int createdBookingId = bookingCreatedResponse.getBookingid();

        Assert.assertTrue(createdBookingId > 0, "Invalid booking ID returned after creation");

        Response retrieveCreatedBooking = bookingClient.getBookingById(createdBookingId)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();

        BookingRequest retrievedBooking = retrieveCreatedBooking.as(BookingRequest.class);
        Assert.assertNotNull(retrievedBooking, "Retrieved booking could not be deserialized");
        Assert.assertNotNull(retrievedBooking.getBookingdates(), "Retrieved booking dates are missing");

        Assert.assertEquals(retrievedBooking.getFirstname(), bookingRequest.getFirstname());
        Assert.assertEquals(retrievedBooking.getLastname(), bookingRequest.getLastname());
        Assert.assertEquals(retrievedBooking.getTotalprice(), bookingRequest.getTotalprice());
        Assert.assertEquals(retrievedBooking.isDepositpaid(), bookingRequest.isDepositpaid());
        Assert.assertEquals(retrievedBooking.getBookingdates().getCheckin(), bookingRequest.getBookingdates().getCheckin());
        Assert.assertEquals(retrievedBooking.getBookingdates().getCheckout(), bookingRequest.getBookingdates().getCheckout());
        Assert.assertEquals(retrievedBooking.getAdditionalneeds(), bookingRequest.getAdditionalneeds());

    }

    @Test(description = "Update an existing booking")
    public void updateExistingBooking(){
        BookingDates initialDates = new BookingDates("2026-12-10", "2026-12-15");
        BookingDates updatedDates = new BookingDates("2026-12-12", "2026-12-18");

        BookingRequest initialBookingRequest = new BookingRequest("Alex",
                "Smith", 250, true, initialDates, "Breakfast");
        BookingRequest updatedBookingRequest = new BookingRequest("Daniel",
                "Johnson", 450, false, updatedDates, "Parking");

        AuthPayload authPayload = new AuthPayload("admin", "password123");
        String token = authClient.authenticate(authPayload)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response().jsonPath().getString("token");

        Assert.assertNotNull(token,  "Authentication token is missing");
        Assert.assertFalse(token.isBlank(), "Authentication token is blank");

        Response bookingCreated = bookingClient.createBooking(initialBookingRequest)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();

        BookingCreatedResponse bookingCreatedResponse = bookingCreated.as(BookingCreatedResponse.class);
        int createdBookingId = bookingCreatedResponse.getBookingid();
        Assert.assertTrue(createdBookingId > 0, "Invalid booking ID returned after creation");

        Response updatedBooking = bookingClient.updateBooking(createdBookingId, updatedBookingRequest, token)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();

        BookingRequest retrievedUpdatedBooking = updatedBooking.as(BookingRequest.class);

        Response retrievedBooking = bookingClient.getBookingById(createdBookingId)
                .then()
                .spec(apiSpecifications.successfulJsonResponse())
                .extract().response();

        BookingRequest updatedBookingResponse = retrievedBooking.as(BookingRequest.class);

        Assert.assertNotNull(retrievedUpdatedBooking, "Retrieved booking could not be deserialized");
        Assert.assertNotNull(retrievedUpdatedBooking.getBookingdates(), "Retrieved booking dates are missing");

        Assert.assertEquals(updatedBookingRequest.getFirstname(), updatedBookingResponse.getFirstname());
        Assert.assertEquals(updatedBookingRequest.getLastname(), updatedBookingResponse.getLastname());
        Assert.assertEquals(updatedBookingRequest.getTotalprice(), updatedBookingResponse.getTotalprice());
        Assert.assertEquals(updatedBookingRequest.isDepositpaid(), updatedBookingResponse.isDepositpaid());
        Assert.assertEquals(updatedBookingRequest.getBookingdates().getCheckin(), updatedBookingResponse.getBookingdates().getCheckin());
        Assert.assertEquals(updatedBookingRequest.getBookingdates().getCheckout(), updatedBookingResponse.getBookingdates().getCheckout());
        Assert.assertEquals(updatedBookingRequest.getAdditionalneeds(), updatedBookingResponse.getAdditionalneeds());
    }

}
