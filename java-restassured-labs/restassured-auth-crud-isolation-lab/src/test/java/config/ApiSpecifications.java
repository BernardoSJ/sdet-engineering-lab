package config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class ApiSpecifications {


    public RequestSpecification setUpRequest() {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri("https://restful-booker.herokuapp.com");
        builder.setAccept("application/json");

        return builder.build();
    }

    public ResponseSpecification successfulJsonResponse(){
        ResponseSpecBuilder builder = new ResponseSpecBuilder();
        builder.expectStatusCode(200);
        builder.expectContentType(ContentType.JSON);

        return builder.build();
    }

}
