# Lab 01 — HTTP Behavior & Response Validation

**Java | REST Assured | TestNG | Maven**

## 1. Overview

This laboratory is part of my ongoing SDET Engineering Labs initiative, focused on strengthening software testing and automation engineering skills through practical experimentation.

The objective of this lab is to understand how to validate REST API responses beyond simply checking HTTP status codes.

Rather than focusing only on whether a request succeeds, the exercises explore how to verify response structures, data types, headers, negative scenarios, and API behavior against expected contracts.

### Main Question

**How can we determine whether an API response is correct beyond receiving HTTP 200 OK?**

---

## 2. Test Application

**Application:** Restful-Booker

**Base URL:** https://restful-booker.herokuapp.com

**API Documentation:** https://restful-booker.herokuapp.com/apidoc/index.html

Restful-Booker is a public API created for practicing API testing. It provides hotel booking operations, including CRUD endpoints and authentication.

The application contains shared test data and periodically resets its database.

Because of this behavior, the tests should avoid relying on fixed booking IDs or assuming that data will remain unchanged between requests.

---

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Programming language |
| REST Assured | HTTP requests and response validation |
| TestNG | Test execution and assertions |
| Maven | Dependency management and build execution |
| JSONPath | Extracting information from JSON responses |
| Java Collections | Working with lists and maps |

---

## 4. Test Scenarios

Four experiments were developed to investigate different aspects of HTTP behavior and response validation.

| Experiment | Endpoint | Objective |
|---|---|---|
| A — Get All Bookings | `GET /booking` | Validate status, response type and JSON structure |
| B — Get Specific Booking | `GET /booking/{id}` | Extract a dynamic ID and validate booking details |
| C — Negative Testing | `GET /booking/{invalidId}` | Validate the response for a nonexistent booking |
| D — Deliberate Validation Failure | `GET /booking` | Understand how a test can fail despite HTTP 200 |

---

## 5. Experiment A — Response Validation

### Objective

Retrieve the available bookings and verify that the response meets the expected structure.

### Initial Approach

The first validation checked only the HTTP status:

```java
Assert.assertEquals(response.getStatusCode(), 200);
```

However, HTTP 200 alone does not demonstrate that the response contains the expected booking information.

A request can succeed at the HTTP level while its payload fails to match the expected structure.

### Improved Validation

The experiment was extended to inspect the response body using JSONPath.

```java
List<Map<String, Object>> bookings = response.jsonPath().getList("$");

Assert.assertFalse(bookings.isEmpty(), "Expected at least one booking");

for (Map<String, Object> booking : bookings) {
    Assert.assertTrue(booking.get("bookingid") instanceof Number, "Missing or invalid bookingid");
}
```

### Findings

The response was evaluated at multiple levels:

- HTTP status code
- Response Content-Type
- JSON array structure
- Collection contents
- Presence and type of `bookingid`

**Key takeaway:** A successful HTTP response is not sufficient evidence that the API meets its expected data contract.

The nonempty-list assertion is dependent on the test environment having available records; it is not a universal requirement for collection endpoints.

---

## 6. Experiment B — Dynamic Booking ID

### Objective

Retrieve the details of an existing booking without using a hardcoded identifier.

### Approach

Instead of manually selecting a booking ID, the test obtains an identifier from the response of the previous endpoint.

```text
GET /booking
      |
      v
Extract bookingid
      |
      v
GET /booking/{bookingid}
      |
      v
Validate booking details
```

### Implementation

```java
List<Map<String, Object>> bookings = response.jsonPath().getList("$");

int bookingId = ((Number) bookings.get(0).get("bookingid")).intValue();

Response specificBookingResponse = given()
        .baseUri("https://restful-booker.herokuapp.com")
        .accept("application/json")
        .pathParam("bookingid", bookingId)
    .when()
        .get("/booking/{bookingid}");
```

The response is then inspected to validate booking properties.

### Validations

| Property | Expected Type |
|---|---|
| `firstname` | String |
| `lastname` | String |
| `totalprice` | Number |
| `bookingdates` | JSON Object |
| `bookingdates.checkin` | String representing a date |
| `bookingdates.checkout` | String representing a date |

Example:

```java
Assert.assertTrue(jsonPath.get("firstname") instanceof String, "Missing or invalid firstname");

Assert.assertTrue(jsonPath.get("totalprice") instanceof Number, "Missing or invalid totalprice");
```

### Date Validation Considerations

Checking that a date is not null does not guarantee it is valid.

For example, a value such as `"invalid-date"` could pass a null check.

An additional improvement is to validate ISO-formatted dates using Java's `LocalDate.parse()` and handle invalid date formats appropriately.

### Findings

Dynamic IDs reduce dependency on hardcoded data.

However, because the public API has shared and periodically reset data, a booking could disappear between the initial request and the subsequent request.

This limitation should be considered when investigating unexpected failures.

**Key takeaway:** Dynamic data improves flexibility, but true test isolation also requires control over test preconditions.

---

## 7. Experiment C — Negative Testing

### Objective

Verify the API's behavior when requesting a booking that does not exist.

### Request

```http
GET /booking/999999
Accept: text/plain
```

The ID was selected for the experiment as a presumed nonexistent booking; the numeric value alone does not guarantee permanent absence.

### Observed Response

```text
HTTP Status: 404
Content-Type: text/plain; charset=utf-8
Body: Not Found
```

### Implementation

```java
Assert.assertEquals(response.getStatusCode(), 404, "Expected HTTP 404");

Assert.assertEquals(response.getContentType(), "text/plain; charset=utf-8");

Assert.assertEquals(response.getBody().asString(), "Not Found");
```

The Content-Type and body assertions document the behavior observed during this experiment. Exact formatting should be checked against the API contract before treating it as a permanent requirement.

### Findings

A test expecting HTTP 404 can pass when the API correctly reports that a requested resource was not found.

This demonstrates an important distinction:

**HTTP error response does not necessarily mean test failure.**

The test result depends on whether the actual behavior meets the expected requirements.

---

## 8. Experiment D — Deliberate Validation Failure

### Objective

Demonstrate that an API can return HTTP 200 while a test fails because the expected response structure does not match the actual payload.

### Experiment

The booking response was validated against an intentionally incorrect property:

```java
Assert.assertTrue(bookings.get(0).containsKey("customerId"), "Expected customerId in booking response");
```

The known response structure uses `bookingid`, not `customerId`.

### Result

The HTTP request succeeded, but the assertion failed.

```text
HTTP response: 200 OK
        |
        v
Payload received
        |
        v
Validate customerId
        |
        v
Property not found
        |
        v
TEST FAILED
```

### Correction

The expected property was restored:

```java
Assert.assertTrue(bookings.get(0).containsKey("bookingid"), "Expected bookingid in booking response");
```

### Important Distinction

This experiment deliberately introduced an incorrect expectation in the automated test.

It did **not** demonstrate a confirmed defect in Restful-Booker.

The failure showed that automated assertions can detect a mismatch, but further investigation is required to determine whether the problem is in the API, the test implementation, or the expected contract.

**Key takeaway:** A failing assertion is evidence of a discrepancy, not automatically evidence of an application defect.

---

## 9. HTTP Errors vs Contract Validation vs Transport Errors

One of the most important lessons from this laboratory was learning to distinguish different types of failures.

| Category | Example | Interpretation |
|---|---|---|
| Transport failure | Connection refused or DNS failure | A usable HTTP response may not have been received |
| HTTP client error | `404 Not Found` | The server returned an HTTP error response |
| HTTP server error | `500 Internal Server Error` | The server or an intermediary encountered a problem |
| Contract mismatch | Expected numeric `bookingid`, received an invalid value | The response did not match an expected requirement |
| Test implementation error | Assertion expects a property not defined by the API | The automated test may contain an incorrect assumption |

A contract mismatch is not restricted to JSON structure. API contracts can also specify status codes, headers, content types, and accepted data values.

A useful debugging sequence is:

```text
Did the client receive an HTTP response?
                  |
          +-------+-------+
          |               |
          No             Yes
          |               |
  Investigate        Inspect status,
  network and        headers and body
  connection              |
  problems                v
                    Compare against
                    expected contract
                          |
                          v
                    Identify the cause
```

---

## 10. Additional Lessons Learned

### Accept vs Content-Type

- `Accept` indicates which response media types the client accepts.
- Request `Content-Type` identifies the format of the content being sent.
- Response `Content-Type` identifies the media type returned by the server.

A GET request without a body generally does not require a JSON request `Content-Type`.

### Validate Before Extracting

Before accessing a collection element or extracting an ID, verify the response status and expected data structure.

This helps avoid misleading Java exceptions, such as an index error when the response contains no elements.

### Prefer Meaningful Assertions

Useful assertions should verify behavior required by the contract.

More assertions do not automatically produce better tests.

### Investigate the Source of Failures

When a test fails, consider whether the cause is:

- Incorrect test expectations
- Unexpected API behavior
- Shared or unstable test data
- An infrastructure or transport problem

Avoid modifying assertions just to make a failing test pass.

---

## 11. Limitations

This laboratory intentionally focuses on response validation and HTTP fundamentals.

It does not yet implement:

- Reusable `RequestSpecification` or `ResponseSpecification`
- Dedicated API client classes
- Serialization and deserialization with POJOs
- Controlled test data creation and cleanup
- Authentication workflows
- JSON Schema validation

The experiments use a public API with shared data. Successful executions do not guarantee that every future execution will encounter identical records or response conditions.

These limitations will guide the design of future laboratories.

---

## 12. Next Steps

The next laboratory will focus on **API Test Architecture & Serialization**.

Planned topics include:

- Reusable request and response specifications
- Separation of API requests from test assertions
- POJOs and Java records
- Serialization and deserialization
- JSON mapping
- Framework responsibilities and maintainability

The objective will be to build upon the HTTP fundamentals explored here without introducing unnecessary abstractions.

---

## 13. Final Takeaways

This laboratory reinforced several engineering principles:

1. HTTP 200 does not guarantee that a response fulfills its expected contract.
2. Negative testing is essential to evaluating API behavior.
3. Dynamic data can reduce reliance on fixed identifiers.
4. Data types and response structures deserve explicit validation.
5. A failing assertion does not automatically prove an API defect.
6. HTTP error responses and network failures are different categories of problems.
7. Reliable API tests require both meaningful expectations and a clear understanding of the environment.

**Final conclusion:**

API testing is not only about sending requests and checking status codes. It is about evaluating whether the service behaves according to clearly defined expectations, understanding discrepancies, and producing meaningful evidence when something fails.

---

## References

- [REST Assured — Official Documentation](https://github.com/rest-assured/rest-assured/wiki/Usage)
- [REST Assured — Getting Started](https://github.com/rest-assured/rest-assured/wiki/GettingStarted)
- [Restful-Booker API](https://restful-booker.herokuapp.com/)
- [Restful-Booker API Documentation](https://restful-booker.herokuapp.com/apidoc/index.html)
- [MDN — HTTP Response Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status)
- [Oracle — LocalDate API](https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/time/LocalDate.html)