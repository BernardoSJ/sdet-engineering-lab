package tests;

import config.ApiSpecifications;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
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

}
