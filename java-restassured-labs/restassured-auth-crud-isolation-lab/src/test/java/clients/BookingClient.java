package clients;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BookingRequest;

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

}
