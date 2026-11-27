package clients;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.BookingRequest;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class BookingClient {

    private static final String BOOKING_ENDPOINT = "/booking";

    private final RequestSpecification requestSpecification;

    public BookingClient(RequestSpecification requestSpecification){
        this.requestSpecification = requestSpecification;
    }

    public  Response getAllBookings() {
        return given()
                .spec(requestSpecification)
                .when()
                .get(BOOKING_ENDPOINT);
    }

    public Response getBookingById(int bookingId) {
        return given()
                .spec(requestSpecification)
                .pathParam("bookingid", bookingId)
                .when()
                .get(BOOKING_ENDPOINT + "/{bookingid}");
    }

    public Response createBooking(BookingRequest bookingPayload) {
        return given()
                .spec(requestSpecification)
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post(BOOKING_ENDPOINT);
    }

    public Response updateBooking(int bookingId, BookingRequest bookingPayload, String token){
        return given()
                .spec(requestSpecification)
                .contentType(ContentType.JSON)
                .pathParam("bookingid",bookingId)
                .cookie("token", token)
                .body(bookingPayload)
                .when()
                .put(BOOKING_ENDPOINT + "/{bookingid}");
    }

    public Response updateBooking(int bookingId, BookingRequest bookingPayload){
        return given()
                .spec(requestSpecification)
                .contentType(ContentType.JSON)
                .pathParam("bookingid",bookingId)
                .body(bookingPayload)
                .when()
                .put(BOOKING_ENDPOINT + "/{bookingid}");
    }

    public Response partiallyUpdateBooking(int bookingId, Map<String, Object> partialPayload, String token){
        return given()
                .spec(requestSpecification)
                .contentType(ContentType.JSON)
                .pathParam("bookingid",bookingId)
                .cookie("token", token)
                .body(partialPayload)
                .when()
                .patch(BOOKING_ENDPOINT + "/{bookingid}");
    }

    public Response deleteBooking(int bookingId, String token){
        return given()
                .spec(requestSpecification)
                .pathParam("bookingid", bookingId)
                .cookie("token", token)
                .when()
                .delete(BOOKING_ENDPOINT + "/{bookingid}");
    }

}
