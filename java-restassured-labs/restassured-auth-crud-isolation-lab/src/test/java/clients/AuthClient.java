package clients;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.AuthPayload;

import static io.restassured.RestAssured.given;

public class AuthClient {

    private static final String AUTH_ENDPOINT = "/auth";
    private final RequestSpecification requestSpecification;

    public AuthClient(RequestSpecification requestSpecification){
        this.requestSpecification = requestSpecification;
    }

    public Response authenticate(AuthPayload authPayload){
        return given()
                .spec(requestSpecification)
                .contentType(ContentType.JSON)
                .body(authPayload)
                .when()
                .post(AUTH_ENDPOINT);
    }

}
