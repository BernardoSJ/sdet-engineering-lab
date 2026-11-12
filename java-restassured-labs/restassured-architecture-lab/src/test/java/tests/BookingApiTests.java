package tests;

import config.ApiSpecifications;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BookingCreatedResponse;
import models.BookingDates;
import models.BookingRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class BookingApiTests {

    RequestSpecification requestSpecification;
    ResponseSpecification responseSpecification;

    @BeforeMethod
    public void setUpRequestAndResponse(){
        ApiSpecifications api = new ApiSpecifications();
        requestSpecification = api.setUpRequest();
        responseSpecification = api.successfulJsonResponse();
    }

    @Test(description = "Get all bookings and verify the response")
    public void getAllBookings(){
        Response response = given()
                .spec(requestSpecification)
                .when()
                .get("/booking")
                .then()
                .spec(responseSpecification).extract().response();


        List<Map<String, Object>> bookings = response.jsonPath().getList("$");
        Assert.assertNotNull(bookings, "Missing bookings list");
        Assert.assertFalse(bookings.isEmpty(), "Expected at least one booking");

        for(Map<String, Object> booking: bookings){
            Assert.assertTrue(booking.get("bookingid") instanceof Number, "Missing or invalid bookingid");
        }
    }

    @Test(description = "Get a specific booking and verify the response")
    public void getSpecificBooking(){
        Response response = given()
                .spec(requestSpecification)
                .when()
                .get("/booking")
                .then()
                .spec(responseSpecification).extract().response();


        List<Map<String, Object>> bookings = response.jsonPath().getList("$");
        Assert.assertNotNull(bookings, "Missing bookings list");
        Assert.assertFalse(bookings.isEmpty(), "Expected at least one booking");

        Object id = bookings.get(0).get("bookingid");
        Assert.assertTrue(id instanceof Number, "Invalid booking ID");
        int bookingId = ((Number) id).intValue();

        Response specificBookingResponse = given()
                .spec(requestSpecification)
                .pathParam("bookingid", bookingId)
                .when()
                .get("/booking/{bookingid}")
                .then()
                .spec(responseSpecification).extract().response();

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

        Response response = given()
                .spec(requestSpecification)
                .contentType(ContentType.JSON)
                .body(bookingRequest)
                .when()
                .post("/booking")
                .then()
                .spec(responseSpecification).extract().response();

        Object bookingId = response.jsonPath().get("bookingid");
        Assert.assertTrue(bookingId instanceof Number, "Missing or invalid bookingid");
        Assert.assertTrue(((Number) bookingId).intValue() > 0, "Booking ID must be positive");

        BookingCreatedResponse createdBooking = response.as(BookingCreatedResponse.class);
        Assert.assertEquals(createdBooking.getBooking().getFirstname(), bookingRequest.getFirstname());
        Assert.assertEquals(createdBooking.getBooking().getLastname(), bookingRequest.getLastname());
        Assert.assertEquals(createdBooking.getBooking().getTotalprice(), bookingRequest.getTotalprice());
        Assert.assertEquals(createdBooking.getBooking().getFirstname(), bookingRequest.getFirstname());
        Assert.assertEquals(createdBooking.getBooking().isDepositpaid(), bookingRequest.isDepositpaid());
        Assert.assertEquals(createdBooking.getBooking().getBookingdates().getCheckin(), bookingRequest.getBookingdates().getCheckin());
        Assert.assertEquals(createdBooking.getBooking().getBookingdates().getCheckout(), bookingRequest.getBookingdates().getCheckout());
        Assert.assertEquals(createdBooking.getBooking().getAdditionalneeds(), bookingRequest.getAdditionalneeds());
    }

}
