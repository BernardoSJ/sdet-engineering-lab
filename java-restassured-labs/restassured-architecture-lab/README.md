# Lab 02 — REST Assured API Architecture & Serialization

**Java | REST Assured | TestNG | Maven | Jackson**

## 1. Overview

This laboratory is part of my SDET Engineering Labs initiative, focused on strengthening software test automation skills through practical experimentation and engineering principles.

The main objective of this lab was to improve the architecture of REST API tests by separating HTTP configuration, API operations, data models, and test validations.

Building upon the previous HTTP response validation laboratory, this exercise introduced reusable specifications, Java object serialization and deserialization, and an API client layer.

### Main Question

**How can we design REST API tests that are maintainable, reusable, and easy to extend without introducing unnecessary complexity?**

---

## 2. Test Application

**Application:** Restful-Booker

**Base URL:** https://restful-booker.herokuapp.com

**API Documentation:** https://restful-booker.herokuapp.com/apidoc/index.html

Restful-Booker is a public API designed for practicing API testing. It exposes endpoints for managing hotel reservations.

The following endpoints were used:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/booking` | Retrieve booking identifiers |
| GET | `/booking/{id}` | Retrieve a specific booking |
| POST | `/booking` | Create a new booking |

The application uses a shared testing environment that periodically resets its data. Therefore, tests should avoid hardcoded booking IDs and unnecessary dependencies between scenarios.

---

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Test implementation and data modeling |
| REST Assured | HTTP requests, specifications and response extraction |
| TestNG | Test execution and assertions |
| Maven | Dependency management |
| Jackson | JSON serialization and deserialization |
| JSONPath | JSON response extraction |

---

## 4. Project Architecture

The project was organized into distinct layers, each with a specific responsibility.

```text
src/test/java/
│
├── clients/
│   └── BookingClient.java
│
├── config/
│   └── ApiSpecifications.java
│
├── models/
│   ├── BookingRequest.java
│   ├── BookingDates.java
│   └── BookingCreatedResponse.java
│
└── tests/
    └── BookingApiTests.java
```

### Responsibilities

**BookingApiTests**

Defines test scenarios, prepares test data, applies expected response validations, and verifies business behavior.

**BookingClient**

Encapsulates HTTP operations related to bookings, including retrieving and creating reservations.

**ApiSpecifications**

Defines reusable HTTP configuration and common response expectations.

**Models**

Represent the JSON request and response structures using Java objects.

### Architecture Flow

```text
BookingApiTests
       |
       | Calls API operations
       v
BookingClient
       |
       | Uses request configuration
       v
ApiSpecifications
       |
       | REST Assured
       v
Restful-Booker API
       |
       | HTTP Response
       v
BookingApiTests
       |
       | Deserialization
       v
Java Models
       |
       | Assertions
       v
Test Result
```

The purpose of this structure was not to introduce multiple layers unnecessarily, but to reduce duplication and clarify responsibilities.

---

## 5. Experiment A — RequestSpecification

### Initial Problem

In the previous laboratory, each test repeated HTTP configuration such as the base URI and accepted response format.

Example:

```java
given()
    .baseUri("https://restful-booker.herokuapp.com")
    .accept(ContentType.JSON)
.when()
    .get("/booking");
```

As the number of tests increases, repeating these configurations makes maintenance more difficult.

### Solution

A reusable `RequestSpecification` was introduced using `RequestSpecBuilder`.

```java
public RequestSpecification setUpRequest() {
    return new RequestSpecBuilder()
            .setBaseUri("https://restful-booker.herokuapp.com")
            .setAccept(ContentType.JSON)
            .build();
}
```

Tests and API clients can now use:

```java
given()
    .spec(requestSpecification)
.when()
    .get("/booking");
```

### Findings

- Common request configuration is defined in one location.
- Tests no longer need to repeat the base URI.
- Different specifications can be created for different services or environments.
- Explicit specifications reduce reliance on mutable global REST Assured configuration.

An important consideration is that using `RequestSpecification` does not automatically guarantee thread safety. Parallel execution still requires careful management of shared mutable state.

**Key takeaway:** Reusable request specifications improve maintainability and configuration clarity, but their lifecycle and ownership must still be considered.

---

## 6. Experiment B — ResponseSpecification

### Objective

Extract common response expectations without moving all business assertions into a shared configuration.

### Implementation

A reusable specification was created for successful JSON responses.

```java
public ResponseSpecification successfulJsonResponse() {
    return new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON)
            .build();
}
```

The specification can be applied as follows:

```java
response.then()
        .spec(responseSpecification);
```

### Design Decision

Only expectations common to the relevant scenarios were included:

- HTTP status 200
- JSON response Content-Type

Specific assertions involving booking names, prices, dates, and other properties remained in the tests.

This distinction is important because common specifications should represent meaningful shared expectations rather than simply collect every repeated assertion.

### Response vs ValidatableResponse

During implementation, an incorrect cast was attempted:

```java
Response response = (Response) given()
        .spec(requestSpecification)
    .when()
        .get("/booking")
    .then()
        .spec(responseSpecification);
```

This failed because the result of the validation chain is a `ValidatableResponse`, not a `Response`.

The correct approach was to extract the original response:

```java
Response response = given()
        .spec(requestSpecification)
    .when()
        .get("/booking")
    .then()
        .spec(responseSpecification)
        .extract()
        .response();
```

**Key takeaway:** Understanding the return types of the REST Assured DSL helps prevent incorrect assumptions and runtime casting errors.

---

## 7. Experiment C — Serialization

### Objective

Create a booking using Java objects instead of manually constructing a JSON request body.

### Initial Data Model

The booking payload was represented using two POJOs.

```text
BookingRequest
├── firstname
├── lastname
├── totalprice
├── depositpaid
├── bookingdates
│   ├── checkin
│   └── checkout
└── additionalneeds
```

The nested `bookingdates` property was modeled using a separate `BookingDates` class.

Example:

```java
BookingDates bookingDates = new BookingDates("2026-12-10", "2026-12-15");

BookingRequest bookingRequest = new BookingRequest("Alex", "Smith", 250, true, bookingDates,"Breakfast");
```

### Serialization Process

Serialization converts a Java object into a representation suitable for transmission, such as JSON.

```text
BookingRequest (Java)
        |
        v
Jackson Serialization
        |
        v
JSON Request Body
        |
        v
POST /booking
```

The object can be passed directly to REST Assured:

```java
Response response = given()
        .spec(requestSpecification)
        .contentType(ContentType.JSON)
        .body(bookingRequest)
    .when()
        .post("/booking");
```

Jackson handles the conversion of the Java properties into JSON.

### Findings

Using Java models provides several benefits:

- Stronger compile-time type checking
- Reusable data structures
- Better readability
- Easier construction and modification of request data
- Reduced reliance on manually concatenated JSON strings

The experiment also demonstrated the importance of having a compatible JSON object mapper available in the project's dependencies.

**Key takeaway:** Serialization allows tests to work with structured Java objects while delegating JSON conversion to a dedicated library.

---

## 8. Experiment D — Deserialization

### Objective

Convert the JSON response from booking creation into a typed Java object.

### Response Structure

The creation endpoint returns a response similar to:

```json
{
  "bookingid": 123,
  "booking": {
    "firstname": "Alex",
    "lastname": "Smith",
    "totalprice": 250,
    "depositpaid": true,
    "bookingdates": {
      "checkin": "2026-12-10",
      "checkout": "2026-12-15"
    },
    "additionalneeds": "Breakfast"
  }
}
```

*The booking ID is illustrative.*

### Model Design

A `BookingCreatedResponse` model was introduced with two properties:

```java
private int bookingid;
private BookingRequest booking;
```

The existing `BookingRequest` model was reused because the nested `booking` object followed the same data structure.

### Deserialization

REST Assured and Jackson were used to convert the response into a Java object:

```java
BookingCreatedResponse createdBooking = response.as(BookingCreatedResponse.class);
```

The deserialized properties could then be accessed using Java methods:

```java
createdBooking.getBookingid();

createdBooking.getBooking().getFirstname();

createdBooking.getBooking().getBookingdates().getCheckin();
```

### Validation

The returned data was compared with the original request:

```java
BookingRequest actualBooking = createdBooking.getBooking();

Assert.assertEquals(actualBooking.getFirstname(), bookingRequest.getFirstname());

Assert.assertEquals(actualBooking.getTotalprice(), bookingRequest.getTotalprice());
```

Additional assertions covered the remaining booking properties and verified that a positive booking ID was returned.

### Findings

- Deserialization allows JSON responses to be represented as typed Java objects.
- Conventional mutable POJOs can use no-argument constructors and setters for deserialization.
- Request and response models can be reused when their contracts are compatible.
- If input and output structures diverge, separate models may become necessary.

**Key takeaway:** Deserialization reduces the need for repeated manual extraction through `JsonPath` and allows response validation using familiar Java types.

---

## 9. Experiment E — BookingClient & Separation of Concerns

### Initial Problem

Although request and response specifications reduced configuration duplication, the tests still contained repeated HTTP operations.

Each test was responsible for constructing requests, selecting endpoints, and executing HTTP methods.

### Solution

A dedicated `BookingClient` was introduced.

Its operations include:

```java
public Response getAllBookings();

public Response getBookingById(int bookingId);

public Response createBooking(BookingRequest bookingPayload);
```

The client receives its request configuration through constructor injection:

```java
public BookingClient(RequestSpecification requestSpecification) {
    this.requestSpecification = requestSpecification;
}
```

This separates HTTP operations from test validations.

### Design Evolution

The first implementation applied a successful `ResponseSpecification` inside every client operation.

However, this would prevent the client from returning responses such as HTTP 404 without failing validation first.

The client was therefore refactored to return the raw REST Assured `Response`.

Example:

```java
public Response getBookingById(int bookingId) {
    return given()
            .spec(requestSpecification)
            .pathParam("bookingid", bookingId)
        .when()
            .get("/booking/{bookingid}");
}
```

The test then decides which response expectations apply.

```java
Response response = bookingClient.getBookingById(bookingId);

response.then()
        .spec(apiSpecifications.successfulJsonResponse());
```

### Why Return Response?

Returning `Response` allows tests to inspect:

- HTTP status codes
- Response headers
- Response bodies
- Different success and error scenarios

Returning a typed model directly can simplify successful business operations, but it also requires additional design decisions about error responses and access to HTTP metadata.

For this laboratory, returning `Response` provided the flexibility required for API testing.

**Key takeaway:** API clients should expose useful HTTP operations without unnecessarily enforcing the expectations of individual test scenarios.

---

## 10. Experiment F — Create & Retrieve Booking

### Objective

Validate that a newly created booking can be retrieved using its generated identifier and that the retrieved data matches the original request.

### Test Flow

```text
Create BookingRequest
        |
        v
POST /booking
        |
        v
Validate HTTP Response
        |
        v
Deserialize BookingCreatedResponse
        |
        v
Extract bookingid
        |
        v
GET /booking/{bookingid}
        |
        v
Deserialize BookingRequest
        |
        v
Compare Retrieved Data
        |
        v
Test Result
```

### Implementation

The test creates its own booking:

```java
BookingDates bookingDates = new BookingDates("2026-12-10", "2026-12-15");

BookingRequest bookingRequest = new BookingRequest("Alex", "Smith", 250, true, bookingDates, "Breakfast");
```

The booking is created through the client:

```java
Response bookingCreated = bookingClient.createBooking(bookingRequest);

bookingCreated.then()
        .spec(apiSpecifications.successfulJsonResponse());

BookingCreatedResponse createdBooking = bookingCreated.as(BookingCreatedResponse.class);
```

The generated identifier is extracted and validated:

```java
int bookingId = createdBooking.getBookingid();

Assert.assertTrue(bookingId > 0, "Invalid booking ID");
```

The booking is retrieved through a second request:

```java
Response retrievedResponse = bookingClient.getBookingById(bookingId);

retrievedResponse.then()
        .spec(apiSpecifications.successfulJsonResponse());

BookingRequest retrievedBooking = retrievedResponse.as(BookingRequest.class);
```

Finally, the test compares the retrieved values against the original request:

```java
Assert.assertEquals(retrievedBooking.getFirstname(), bookingRequest.getFirstname());

Assert.assertEquals(retrievedBooking.getLastname(), bookingRequest.getLastname());

Assert.assertEquals(retrievedBooking.getTotalprice(), bookingRequest.getTotalprice());

Assert.assertEquals(retrievedBooking.getBookingdates().getCheckin(), bookingRequest.getBookingdates().getCheckin());
```

The full implementation validates the remaining booking properties as well.

### Why Retrieve After Creating?

Validating the immediate POST response does not necessarily demonstrate that a resource can subsequently be retrieved with the same data.

The POST-to-GET scenario verifies consistency between the creation and retrieval operations.

This can help detect issues such as incorrect field mapping or inconsistent data returned by different endpoints.

However, a successful GET does not independently prove database-level persistence or long-term durability.

### Test Independence

The test generates its own booking and uses the returned ID instead of depending on a separate test.

This reduces coupling between test methods.

Because Restful-Booker is a shared environment, external changes or database resets may still affect execution.

**Key takeaway:** A create-and-retrieve test provides additional evidence of API behavior beyond validating only the immediate creation response.

---

## 11. Final Test Coverage

The final laboratory includes the following scenarios:

| Test | Main Validation |
|---|---|
| `getAllBookings()` | Reusable specifications and booking ID structure |
| `getSpecificBooking()` | Dynamic ID extraction and response fields |
| `createNewBooking()` | Serialization and deserialization of creation response |
| `createAndRetrieveBooking()` | Creation, retrieval and data consistency |

All tests were reported as passing after the architectural refactor.

---

## 12. Architecture Decisions & Trade-offs

Several decisions were made to keep the implementation focused and maintainable.

### Specifications vs Business Assertions

Common HTTP expectations were placed in reusable specifications.

Field-level and scenario-specific validations remained in the tests.

### Response vs Typed Models

The API client returns `Response` so that tests can inspect both successful and unsuccessful HTTP outcomes.

Models are used when typed data access improves validation.

### Reusing Request Models

`BookingRequest` was reused for the nested booking response because the current data structures are compatible.

Separate response models may be necessary if the API contracts evolve independently.

### Avoiding Overengineering

The project deliberately avoids introducing unnecessary factories, interfaces, generic validators, and dependency injection frameworks.

The architecture focuses on solving actual duplication and responsibility problems observed during development.

---

## 13. Limitations

This laboratory intentionally focuses on API architecture and data mapping.

The following areas remain outside its scope:

- Authentication and authorization
- Complete CRUD lifecycle
- Test data cleanup
- JSON Schema validation
- Parallel execution
- Retry strategies
- Environment management
- Database-level verification

The public API has shared and periodically reset data, which may introduce environmental instability.

Also, the project uses conventional POJOs for serialization and deserialization. Java records were considered as a possible alternative but were not part of the final implementation.

---

## 14. Lessons Learned

- `RequestSpecification` centralizes reusable HTTP configuration.
- `ResponseSpecification` helps define shared expectations without duplicating assertions.
- REST Assured's `Response` and `ValidatableResponse` represent different stages of the API DSL.
- Serialization transforms Java objects into JSON representations.
- Deserialization reconstructs Java objects from JSON.
- Jackson reduces manual JSON construction and parsing.
- Typed Java models improve readability and help identify type mismatches.
- Constructor injection makes dependencies explicit.
- API clients separate HTTP operations from test scenarios.
- Returning `Response` allows tests to inspect complete HTTP results.
- Response validation should depend on the scenario rather than be imposed globally by the client.
- Dynamic booking IDs reduce dependency on hardcoded data.
- Creating and retrieving a resource provides additional evidence of data consistency.
- Good architecture is based on clear responsibilities and justified abstractions, not on the number of classes or patterns used.

---

## References

- [REST Assured — Official Usage Guide](https://github.com/rest-assured/rest-assured/wiki/Usage)
- [REST Assured — Getting Started](https://github.com/rest-assured/rest-assured/wiki/GettingStarted)
- [Restful-Booker API](https://restful-booker.herokuapp.com/)
- [Restful-Booker API Documentation](https://restful-booker.herokuapp.com/apidoc/index.html)
- [Jackson Databind](https://github.com/FasterXML/jackson-databind)
- [TestNG Documentation](https://testng.org/)

---

## Final Conclusion

This laboratory demonstrated the evolution from individual REST API tests with repeated HTTP logic toward a layered architecture with reusable specifications, dedicated API operations, and typed Java models.

The most important learning was not simply how to use REST Assured or Jackson, but how to make engineering decisions about responsibilities, abstraction boundaries, and response validation.

**The objective was to build a small, understandable, and maintainable API testing structure while avoiding unnecessary complexity.**