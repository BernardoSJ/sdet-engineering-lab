package tests;

import io.restassured.internal.mapping.JsonbMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static io.restassured.RestAssured.given;

public class BookingTests {

    @Test(description = "Get all bookings and verify the response")
    public void getAllBookings() {
        Response response = given()
                .baseUri("https://restful-booker.herokuapp.com")
                .header("Content-Type", "application/json")
                .accept("application/json")
                .when()
                .get("/booking");
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 but got " + response.getStatusCode());

        Assert.assertTrue(response.getContentType()
                        .toLowerCase(Locale.ROOT)
                        .startsWith("application/json"),
                "Expected JSON response");

        List<Map<String, Object>> bookings = response.jsonPath().getList("$");

        Assert.assertFalse(bookings.isEmpty(), "Expected at least one booking");

        for (Map<String, Object> booking : bookings) {
            Assert.assertTrue(booking.get("bookingid") instanceof Number, "Missing or invalid bookingid");
        }
    }

    @Test(description = "Get a specific booking and verify the response")
    public void getSpecificBooking() {
        Response response = given()
                .baseUri("https://restful-booker.herokuapp.com")
                .header("Content-Type", "application/json")
                .accept("application/json")
                .when()
                .get("/booking");
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to retrieve booking IDs");

        List<Map<String, Object>> bookings = response.jsonPath().getList("$");
        Assert.assertNotNull(bookings, "Missing bookings list");
        Assert.assertFalse(bookings.isEmpty(), "No bookings available");

        Object id = bookings.get(0).get("bookingid");
        Assert.assertTrue(id instanceof Number, "Invalid booking ID");
        int bookingId = ((Number) id).intValue();

        Response specificBookingResponse = given()
                .baseUri("https://restful-booker.herokuapp.com")
                .header("Content-Type", "application/json")
                .accept("application/json")
                .pathParam("bookingid", bookingId)
                .when()
                .get("/booking/{bookingid}");

        Assert.assertEquals(specificBookingResponse.getStatusCode(), 200, "Expected status code 200 but got " + specificBookingResponse.getStatusCode());
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

    @Test(description = "Get a non-existent booking and verify the response")
    public void getNonExistentBooking() {
        int nonExistentBookingId = 999999;

        Response response = given()
                .baseUri("https://restful-booker.herokuapp.com")
                .accept("text/plain")
                .pathParam("bookingid", nonExistentBookingId)
                .when()
                .get("/booking/{bookingid}");

        Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404 for non-existent booking but got " + response.getStatusCode());
        Assert.assertEquals(response.getContentType(), "text/plain; charset=utf-8", "Expected text/plain response for non-existent booking");
        Assert.assertEquals(response.getBody().asString(), "Not Found", "Expected 'Not Found' message for non-existent booking");
    }

    @Test(description = "Get deliberate contract failure and verify the response")
    public void getDeliberateContractFailure() {
        Response response = given()
                .baseUri("https://restful-booker.herokuapp.com")
                .header("Content-Type", "application/json")
                .accept("application/json")
                .when()
                .get("/booking");
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 but got " + response.getStatusCode());

        List<Map<String, Object>> bookings = response.jsonPath().getList("$");
        Assert.assertFalse(bookings.isEmpty(), "Expected at least one booking");

        Assert.assertTrue(bookings.get(0).containsKey("customerId"), "Expected customerId in booking response");
    }
}
