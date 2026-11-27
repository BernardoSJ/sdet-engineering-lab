# Lab 03 — Authentication, CRUD Lifecycle & Test Isolation

**Java | REST Assured | TestNG | Maven | Jackson**

## 1. Overview

This laboratory is part of my SDET Engineering Labs initiative, focused on strengthening test automation skills through practical experimentation, failure diagnosis, and software engineering principles.

Building upon the architecture developed in Lab 02, this laboratory extends the API testing implementation to support authentication, complete CRUD operations, test data management, and automated resource cleanup.

Rather than focusing exclusively on successful HTTP requests, this laboratory explores how automated tests manage their resources, preserve diagnostic information, and behave when unexpected failures occur.

### Main Question

**How can we design API tests that manage their own data, validate authenticated operations, and attempt resource cleanup even when failures occur?**

---

## 2. Test Application

**Application:** Restful-Booker

**Base URL:** https://restful-booker.herokuapp.com

**API Documentation:** https://restful-booker.herokuapp.com/apidoc/index.html

Restful-Booker is a public API designed for practicing API testing through hotel reservation management.

The following endpoints were used:

| Method | Endpoint | Purpose | Authentication Required |
|---|---|---|---|
| POST | `/auth` | Generate an authentication token | No existing token required |
| GET | `/booking` | Retrieve booking identifiers | No |
| GET | `/booking/{id}` | Retrieve a specific booking | No |
| POST | `/booking` | Create a booking | No |
| PUT | `/booking/{id}` | Fully update a booking | Yes |
| PATCH | `/booking/{id}` | Partially update a booking | Yes |
| DELETE | `/booking/{id}` | Delete a booking | Yes |

The application uses a shared environment that periodically resets its data. This limitation was considered when designing test preconditions and resource cleanup.

---

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Test implementation, object modeling and exception handling |
| REST Assured | HTTP requests, authentication cookies and response validation |
| TestNG | Test execution, assertions and lifecycle management |
| Maven | Dependency management |
| Jackson | JSON serialization and deserialization |
| JSONPath | Token and response data extraction |

---

## 4. Project Architecture

The architecture established in Lab 02 was extended rather than redesigned.

The following structure represents the main components of the laboratory:

```text
restassured-auth-crud-isolation-lab/
│
├── pom.xml
├── README.md
│
└── src/test/java/
    │
    ├── clients/
    │   ├── AuthClient.java
    │   └── BookingClient.java
    │
    ├── config/
    │   └── ApiSpecifications.java
    │
    ├── models/
    │   ├── AuthPayload.java
    │   ├── BookingRequest.java
    │   ├── BookingDates.java
    │   └── BookingCreatedResponse.java
    │
    └── tests/
        ├── AuthenticationTests.java
        ├── BookingLifeCycleTests.java
        │
        └── experiments/
            └── CleanupFailureHandlingDemo.java
```

### Component Responsibilities

| Component | Responsibility |
|---|---|
| `AuthClient` | Execute authentication requests |
| `BookingClient` | Execute booking-related HTTP operations |
| `ApiSpecifications` | Provide reusable request configuration and response expectations |
| `Models` | Represent request and response data using Java objects |
| `AuthenticationTests` | Validate authentication behavior |
| `BookingLifeCycleTests` | Validate CRUD operations and manage test resources |
| Cleanup experiments | Demonstrate Java exception handling and failure preservation |

The API clients continue returning REST Assured `Response` objects, allowing tests to determine which HTTP expectations apply to each scenario.

No additional framework-level abstractions were introduced without an identified requirement.

---

## 5. Experiment A — Authentication & Token Management

### Objective

Understand how authentication tokens are generated, validated, and used to execute protected API operations.

### Implementation

A dedicated `AuthClient` was introduced using constructor injection.

```java
public class AuthClient {

    private static final String AUTH_ENDPOINT = "/auth";

    private final RequestSpecification requestSpecification;

    public AuthClient(RequestSpecification requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public Response authenticate(AuthPayload authPayload) {
        return given()
                .spec(requestSpecification)
                .contentType(ContentType.JSON)
                .body(authPayload)
            .when()
                .post(AUTH_ENDPOINT);
    }
}
```

The authentication payload was represented using a Java model instead of a manually constructed JSON string.

### Valid Authentication

The positive authentication scenario verifies that valid credentials return a non-null and non-blank token.

```java
String token = authClient.authenticate(authPayload)
        .then()
        .spec(apiSpecifications.successfulJsonResponse())
        .extract()
        .jsonPath()
        .getString("token");

Assert.assertNotNull(token, "Authentication token is missing");

Assert.assertFalse(token.isBlank(), "Authentication token is blank");
```

### Invalid Authentication

Restful-Booker has a particular authentication behavior: invalid credentials can produce an HTTP 200 response containing a business-level error.

```json
{
    "reason": "Bad credentials"
}
```

The negative test validates both the rejection reason and the absence of an authentication token.

```java
Assert.assertEquals(response.jsonPath().getString("reason"), "Bad credentials");

Assert.assertNull(response.jsonPath().getString("token"), "Token should not be returned");
```

Checking only the absence of a token would be insufficient because an unrelated JSON response could also satisfy that assertion.

### Authentication vs Authorization

An important distinction was established:

- Authentication determines the identity associated with a request.
- Authorization determines whether the requested operation is permitted.

Receiving a token does not independently demonstrate that every protected operation will succeed.

### Token Lifecycle

Tokens are obtained within the scope of the test that requires them rather than stored in a mutable static variable shared across the suite.

For protected operations, the token is passed explicitly:

```java
.cookie("token", token)
```

This makes authentication dependencies visible and reduces unnecessary shared state.

**Key takeaway:** Successful authentication requires more than checking an HTTP status code. Tests should validate the expected authentication outcome and verify that credentials can be used appropriately.

---

## 6. Experiment B — PUT: Full Resource Update

### Objective

Validate that an authenticated request can update a complete booking and that the updated values can subsequently be retrieved.

### Test Flow

```text
POST /auth
    |
    v
Obtain authentication token
    |
    v
POST /booking
    |
    v
Capture booking ID
    |
    v
PUT /booking/{id}
    |
    v
Validate update response
    |
    v
GET /booking/{id}
    |
    v
Compare retrieved data with expected data
```

### BookingClient Implementation

```java
public Response updateBooking(int bookingId, BookingRequest bookingPayload, String token) {
    return given()
            .spec(requestSpecification)
            .contentType(ContentType.JSON)
            .pathParam("bookingid", bookingId)
            .cookie("token", token)
            .body(bookingPayload)
        .when()
            .put(BOOKING_ENDPOINT + "/{bookingid}");
}
```

### Test Data

| Property | Original Value | Updated Value |
|---|---|---|
| firstname | Alex | Daniel |
| lastname | Smith | Johnson |
| totalprice | 250 | 450 |
| depositpaid | true | false |
| checkin | 2026-12-10 | 2026-12-12 |
| checkout | 2026-12-15 | 2026-12-18 |
| additionalneeds | Breakfast | Parking |

### Identified Problem — False Positive Assertions

During the initial implementation, the PUT response was compared directly against the subsequent GET response.

Although this verified consistency between the endpoints, it did not necessarily demonstrate that the requested update was applied.

Consider the following hypothetical outcome:

| Source | firstname |
|---|---|
| Expected updated value | Daniel |
| PUT response | Alex |
| GET response | Alex |

Comparing only the two responses would produce a passing assertion even though the requested modification was not applied.

### Improved Validation Strategy

The expected update payload became the independent source of expected values.

```java
Assert.assertEquals(putResponseBooking.getFirstname(), updatedBookingRequest.getFirstname());

Assert.assertEquals(getResponseBooking.getFirstname(), updatedBookingRequest.getFirstname());
```

The same comparison strategy applies to the remaining booking properties.

This separates two objectives:

- Validate the immediate update response.
- Validate the subsequently retrievable resource state.

A successful GET provides additional evidence of the externally observable state, but it does not independently prove database-level persistence.

**Key takeaway:** Test assertions should compare actual results against meaningful expected values rather than rely exclusively on consistency between system responses.

---

## 7. Experiment C — PATCH: Partial Resource Update

### Objective

Validate that a partial update modifies only the intended properties while preserving the remaining resource state.

### Payload Design

Unlike PUT, the PATCH experiment did not require a complete `BookingRequest` object.

A `Map<String, Object>` was used to represent only the properties included in the partial update.

```java
Map<String, Object> partialPayload = new HashMap<>();

partialPayload.put("firstname", "Michael");
partialPayload.put("totalprice", 350);
```

This produces a payload equivalent to:

```json
{
    "firstname": "Michael",
    "totalprice": 350
}
```

### Why Not Reuse the Complete BookingRequest?

A partially initialized POJO could contain default values such as:

- `null` for object references.
- `0` for integer properties.
- `false` for boolean properties.

Depending on the serialization configuration and API contract, including these properties in a PATCH request could overwrite existing data or cause request validation failures.

The primary concern is data integrity, not Java object memory consumption.

### BookingClient Implementation

```java
public Response partiallyUpdateBooking(int bookingId, Map<String, Object> partialPayload, String token) {
    return given()
            .spec(requestSpecification)
            .contentType(ContentType.JSON)
            .pathParam("bookingid", bookingId)
            .cookie("token", token)
            .body(partialPayload)
        .when()
            .patch(BOOKING_ENDPOINT + "/{bookingid}");
}
```

### Validation Strategy

The test uses two independent sources of expected values.

| Property | Expected Behavior | Expected Value |
|---|---|---|
| firstname | Modified | Michael |
| totalprice | Modified | 350 |
| lastname | Preserved | Smith |
| depositpaid | Preserved | true |
| checkin | Preserved | 2026-12-10 |
| checkout | Preserved | 2026-12-15 |
| additionalneeds | Preserved | Breakfast |

Modified properties are compared against the partial payload.

```java
Assert.assertEquals(retrievedBooking.getFirstname(), partialPayload.get("firstname"));

Assert.assertEquals(retrievedBooking.getTotalprice(),((Number) partialPayload.get("totalprice")).intValue());
```

Unmodified properties are compared against the original booking.

```java
Assert.assertEquals(retrievedBooking.getLastname(), initialBookingRequest.getLastname());

Assert.assertEquals(retrievedBooking.isDepositpaid(), initialBookingRequest.isDepositpaid());
```

The PATCH response and the subsequent GET response are both validated.

### Findings

A successful partial update requires two conditions:

1. Requested modifications are applied.
2. Properties outside the requested update remain unchanged.

If either condition fails, the expected behavior has not been satisfied.

**Key takeaway:** Partial update testing must validate both intended changes and unintended side effects.

---

## 8. Experiment D — DELETE: Resource Deletion

### Objective

Validate that an authenticated DELETE request removes a booking from the externally accessible API state.

### BookingClient Implementation

```java
public Response deleteBooking(int bookingId, String token) {
    return given()
            .spec(requestSpecification)
            .pathParam("bookingid", bookingId)
            .cookie("token", token)
        .when()
            .delete(BOOKING_ENDPOINT + "/{bookingid}");
}
```

### Test Flow

```text
POST /auth
    |
    v
POST /booking
    |
    v
Capture booking ID
    |
    v
DELETE /booking/{id}
    |
    v
Validate deletion response
    |
    v
GET /booking/{id}
    |
    v
Verify resource is no longer accessible
```

### API-Specific HTTP Behavior

Restful-Booker returns HTTP 201 with the text `Created` for a successful DELETE operation.

Although HTTP 204 is commonly associated with successful deletion in other APIs, the test validates the documented and implemented behavior of this application.

```java
Response deleteResponse = bookingClient.deleteBooking(bookingId, token);

Assert.assertEquals(deleteResponse.statusCode(), 201);
Assert.assertEquals(deleteResponse.body().asString(), "Created");
```

A subsequent GET verifies that the resource is no longer accessible.

```java
Response retrievedBooking = bookingClient.getBookingById(bookingId);

Assert.assertEquals(retrievedBooking.statusCode(), 404);
```

The successful JSON response specification was deliberately not applied to these operations because their expected responses differ from HTTP 200 with JSON.

**Key takeaway:** A successful deletion response should be complemented by a state verification when resource removal is the behavior under test.

---

## 9. Experiment E — Test Isolation & Resource Ownership

### Initial Problem

The CRUD scenarios initially created bookings without automatically removing them after execution.

This introduced a resource management problem.

```text
Create booking
    |
    v
Execute operation
    |
    v
Assertion fails
    |
    v
Test terminates
    |
    v
Booking may remain in the environment
```

Adding a DELETE request at the end of a test method is insufficient when an earlier assertion or exception interrupts execution.

### Resource Ownership Principles

The laboratory established the following principles:

- Mutation tests create their own bookings.
- Tests do not rely on another test having executed previously.
- Generated booking IDs are captured dynamically.
- Only resources created by the current test are eligible for cleanup.
- Cleanup is attempted even when functional assertions fail.
- Cleanup failures must remain visible.

Read-only scenarios can inspect existing bookings without taking ownership of those resources.

**Key takeaway:** Test independence includes managing the lifecycle of test data, not simply avoiding execution-order dependencies.

---

## 10. Experiment F — Cleanup with try/finally

### Objective

Investigate how Java executes cleanup logic when an assertion fails.

The initial approach used a `try/finally` structure.

```java
int bookingId = 0;

try {
    // Create booking and capture its ID.
    // Execute API operations.
    // Validate expected behavior.
} finally {
    if (bookingId > 0) {
        // Attempt resource cleanup.
    }
}
```

This allows cleanup to be attempted when execution leaves the `try` block because of an assertion failure or exception.

However, the experiment revealed an additional problem.

### Identified Problem — Exception Masking

A deliberate assertion failure was introduced in the test body, followed by another failure inside the cleanup block.

```java
try {
    Assert.fail("Firstname validation failed");
} finally {
    Assert.fail("DELETE failed during cleanup");
}
```

During execution, the console reported only the cleanup failure.

The original functional failure was no longer visible as the primary exception.

This behavior is defined by Java's exception-handling semantics: when a `finally` block completes abruptly, it can replace the original reason for abrupt completion.

### Why This Matters

In an automated test suite, losing the original failure can make diagnosis significantly harder.

An application defect and a cleanup failure are different problems. Both should remain available for investigation.

**Key takeaway:** Executing cleanup is not sufficient if the cleanup mechanism destroys the evidence needed to diagnose the original failure.

---

## 11. Experiment G — Suppressed Exceptions

### Objective

Preserve the original test failure while retaining evidence of a secondary cleanup failure.

Java provides the `Throwable.addSuppressed()` method for associating secondary failures with a primary failure.

### Simplified Experimental Implementation

```java
@Test
public void verifyFailureHandling() {

    Throwable primaryFailure = null;

    try {
        Assert.fail("Firstname validation failed");

    } catch (Throwable failure) {
        primaryFailure = failure;
        throw failure;

    } finally {
        try {
            Assert.fail("DELETE failed during cleanup");

        } catch (Throwable cleanupFailure) {

            if (primaryFailure != null) {
                primaryFailure.addSuppressed(cleanupFailure);
            } else {
                throw cleanupFailure;
            }
        }
    }
}
```

### Expected Diagnostic Output

```text
java.lang.AssertionError:
Firstname validation failed

    Suppressed: java.lang.AssertionError:
    DELETE failed during cleanup
```

*Illustrative output. The exact formatting depends on the test runner.*

The original assertion remains the primary failure while the cleanup failure is retained as supplementary diagnostic information.

### Throwable vs Exception

The experiment also demonstrated an important Java distinction.

```text
Throwable
├── Exception
└── Error
    └── AssertionError
```

Catching `Exception` alone does not capture an `AssertionError`.

In this controlled experiment, `Throwable` was used specifically to preserve and propagate failures, rather than suppressing or ignoring them.

### Failure Handling Matrix

For the `try/finally` strategy:

| Test Execution | Cleanup | Expected Outcome |
|---|---|---|
| Successful | Successful | Test passes |
| Failed | Successful | Original failure is reported |
| Successful | Failed | Cleanup failure is reported |
| Failed | Failed | Original failure is preserved with the cleanup failure suppressed |

**Key takeaway:** Failure-handling mechanisms should preserve diagnostic context rather than replace one failure with another.

---

## 12. Experiment H — Automated Teardown with TestNG

### Objective

Centralize resource cleanup and separate test logic from lifecycle management.

After investigating `try/finally`, TestNG lifecycle annotations were introduced as the primary cleanup strategy for functional tests.

### Lifecycle Design

```text
@BeforeMethod
    |
    | Reset resource state
    | Initialize API clients
    v
@Test
    |
    | Authenticate when required
    | Create owned resource
    | Execute API operations
    | Validate expected behavior
    v
@AfterMethod(alwaysRun = true)
    |
    | Check resource ownership
    | Attempt DELETE
    | Verify resource absence
    v
Execution Report
```

### Setup Implementation

```java
private int bookingId;
private String token;

@BeforeMethod
public void setUpRequestAndResponse() {

    bookingId = 0;
    token = "";

    apiSpecifications = new ApiSpecifications();

    RequestSpecification requestSpecification = apiSpecifications.setUpRequest();

    bookingClient = new BookingClient(requestSpecification);
    authClient = new AuthClient(requestSpecification);
}
```

The instance attributes allow the test and teardown method to access the same resource information during sequential execution.

### Resource Registration

As soon as a valid booking identifier becomes available, it is registered for cleanup.

```java
BookingCreatedResponse createdBooking = response.as(BookingCreatedResponse.class);

bookingId = createdBooking.getBookingid();

Assert.assertTrue(bookingId > 0, "Invalid booking ID returned after creation");
```

Registering the ID before additional business assertions reduces the risk of losing the reference when a later validation fails.

### Final Cleanup Strategy

The following example represents the final teardown approach:

```java
@AfterMethod(alwaysRun = true)
public void cleanUp() {

    if (bookingId <= 0) {
        return;
    }

    Assert.assertNotNull(token, "Cleanup token is missing");

    Assert.assertFalse(token.isBlank(), "Cleanup token is blank");

    Response deleteResponse = bookingClient.deleteBooking(bookingId, token);

    Assert.assertEquals(deleteResponse.statusCode(), 201, "Cleanup failed for booking ID: " + bookingId);

    Response retrievedBooking = bookingClient.getBookingById(bookingId);

    Assert.assertEquals(retrievedBooking.statusCode(), 404, "Booking still accessible after cleanup: " + bookingId);

    System.out.println("[CLEANUP] Booking " + bookingId + " deleted");
    bookingId = 0;
}
```

The `alwaysRun = true` configuration allows TestNG to execute the teardown even when the preceding test fails.

Unlike the inline `finally` strategy, a failing TestNG teardown can be reported as a configuration failure separately from the functional test result.

A passing test method does not necessarily mean its associated configuration methods completed successfully.

**Key takeaway:** Lifecycle annotations improve separation of concerns, but the cleanup behavior itself must also be validated.

---

## 13. Experiment I — Debugging Variable Shadowing

### Identified Problem

During the initial teardown implementation, the test suite reported successful execution, but code inspection revealed that some scenarios were silently skipping cleanup.

The class contained an instance attribute:

```java
private String token;
```

However, several test methods declared another variable with the same name:

```java
String token = authClient.authenticate(authPayload)
        .then()
        .spec(apiSpecifications.successfulJsonResponse())
        .extract()
        .jsonPath()
        .getString("token");
```

This introduced variable shadowing.

The local variable contained the authentication token, while the instance attribute remained empty.

### Root Cause

```text
@BeforeMethod
    |
    | this.token = ""
    v
@Test
    |
    | String token = validToken
    | Protected operation succeeds
    v
@AfterMethod
    |
    | Reads this.token
    | Token is empty
    v
Cleanup is skipped
```

The original teardown condition was:

```java
if (bookingId > 0 && !token.isEmpty()) {
    // Delete resource
}
```

Because the instance token was empty, the cleanup logic was never executed for the affected scenarios.

### Solution

The token was explicitly assigned to the instance attribute.

```java
this.token = authClient.authenticate(authPayload)
        .then()
        .spec(apiSpecifications.successfulJsonResponse())
        .extract()
        .jsonPath()
        .getString("token");
```

Repeated authentication logic was also identified as a valid opportunity for reuse through a helper method.

```java
private void authenticateUser() {

    AuthPayload authPayload = new AuthPayload("admin", "password123");

    this.token = authClient.authenticate(authPayload)
            .then()
            .spec(apiSpecifications.successfulJsonResponse())
            .extract()
            .jsonPath()
            .getString("token");

    Assert.assertNotNull(token, "Authentication token is missing");
    Assert.assertFalse(token.isBlank(), "Authentication token is blank");
}
```

The credentials shown are public demonstration credentials for Restful-Booker, not production secrets.

Additionally, cleanup validation was strengthened so that an existing booking without a usable token produces a visible configuration failure rather than silently skipping cleanup.

### Observed Execution Evidence

Before identifying the shadowing issue, the suite reported:

```text
===============================================
Default Suite
Total tests run: 8, Passes: 8, Failures: 0, Skips: 0
===============================================

Process finished with exit code 0
```

Despite the passing test results, inspection of the code revealed that several tests were not invoking the intended cleanup operation.

After correcting the issue, successful cleanup behavior was confirmed during subsequent execution.

**Key takeaway: A completely green test suite does not necessarily mean the automation infrastructure is behaving correctly.**

Test setup, resource registration, teardown, and diagnostic mechanisms also require verification.

---

## 14. Preventing Duplicate Resource Deletion

### Problem

The functional DELETE scenario removes its booking as part of the test itself.

A shared teardown could therefore attempt to delete the same booking again.

### Solution

After the functional DELETE and subsequent GET confirm that the resource is no longer accessible, the registered booking ID is cleared.

```java
Assert.assertEquals(retrievedBooking.statusCode(), 404);

bookingId = 0;
```

The teardown then recognizes that no resource remains registered for cleanup.

### Resource State

```text
bookingId = 0
    |
    | Create booking
    v
bookingId = generatedId
    |
    | Delete and verify absence
    v
bookingId = 0
```

The identifier should not be cleared merely because a test has finished or a cleanup attempt has failed.

It should only be cleared after the resource has been successfully accounted for.

A limitation remains when deletion succeeds but its subsequent verification fails: teardown may attempt another deletion because the resource is still registered.

**Key takeaway:** Cleanup state should represent the resource lifecycle rather than simply the execution status of the test.

---

## 15. Experiment J — Unauthorized Resource Update

### Objective

Validate that an unauthenticated update request is rejected and does not modify the existing resource.

### Client Design

Method overloading was introduced to distinguish between authenticated and unauthenticated update requests.

```java
public Response updateBooking(int bookingId, BookingRequest bookingPayload, String token);
```

```java
public Response updateBooking(int bookingId, BookingRequest bookingPayload);
```

The unauthenticated operation must omit authentication credentials rather than send an empty token.

### Test Flow

```text
Obtain token for cleanup purposes
    |
    v
Create booking
    |
    v
Register booking ID
    |
    v
Execute PUT without authentication
    |
    v
Verify HTTP 403
    |
    v
GET /booking/{id}
    |
    v
Compare against original booking
    |
    v
Authenticated cleanup
```

Obtaining an authentication token for cleanup does not invalidate the negative scenario, provided that the PUT request does not include that token or other authentication credentials.

### HTTP Validation

```java
Response updatedBooking = bookingClient.updateBooking(bookingId, updatedBookingRequest);

Assert.assertEquals(updatedBooking.statusCode(), 403, "Unauthenticated booking update should be rejected");
```

### State Validation

After the rejected request, the booking is retrieved and compared against its original values.

```java
Assert.assertEquals(retrievedBookingResponse.getFirstname(), initialBookingRequest.getFirstname());

Assert.assertEquals(retrievedBookingResponse.getTotalprice(), initialBookingRequest.getTotalprice());
```

The remaining booking properties are also validated.

### Security Considerations

Checking only HTTP 403 would not be sufficient to demonstrate that the protected operation was prevented.

| HTTP Response | Retrieved Resource State | Interpretation |
|---|---|---|
| 403 | Original values | Expected behavior |
| 403 | Modified values | Possible access control defect |
| 200 | Modified values | Unauthorized modification |
| 200 | Original values | Unexpected HTTP response |

If a service rejects a request while still applying the requested modification, this could indicate a security defect involving access control and data integrity.

This was investigated as a hypothetical failure condition, not as a confirmed vulnerability in Restful-Booker.

**Key takeaway:** Negative security tests should validate both the rejection response and the absence of unauthorized side effects.

---

## 16. Test Isolation vs Parallel Execution

An alternative approach using `ThreadLocal<Integer>` was considered for storing booking identifiers.

```java
private final ThreadLocal<Integer> bookingId = new ThreadLocal<>();
```

Each thread could maintain its own identifier.

```java
bookingId.set(createdBookingId);
```

The teardown would retrieve and remove the thread-specific value.

```java
try {
    Integer id = bookingId.get();

    if (id != null && id > 0) {
        // Attempt resource cleanup.
    }
} finally {
    bookingId.remove();
}
```

However, this laboratory deliberately retains sequential execution and instance-level resource tracking.

Introducing `ThreadLocal` would add complexity without addressing a current execution requirement.

Furthermore, isolating the booking ID alone would not make the complete framework thread-safe. Authentication state, API clients, request specifications, and execution lifecycle would also require consideration.

### Strategy Comparison

| Characteristic | try/finally | TestNG @AfterMethod | ThreadLocal + @AfterMethod |
|---|---|---|---|
| Cleanup after assertion failure | Supported | Supported | Supported |
| Centralized lifecycle management | Limited | Supported | Supported |
| Resource ID isolation by thread | Possible with local variables | Not guaranteed | Supported |
| Implementation complexity | Medium | Low | Medium |
| Selected for final functional tests | No | Yes | No |

**Key takeaway:** Architectural decisions should address existing requirements rather than introduce abstractions solely for potential future use.

---

## 17. Final Test Coverage

### Authentication Scenarios

| Test | Objective |
|---|---|
| `validAuthentication()` | Validate token generation |
| `invalidAuthentication()` | Validate credential rejection |

### Booking Lifecycle Scenarios

| Test | Objective |
|---|---|
| `getAllBookings()` | Validate booking collection structure |
| `getSpecificBooking()` | Retrieve an existing booking dynamically |
| `createNewBooking()` | Validate booking creation |
| `createAndRetrieveBooking()` | Validate creation and subsequent retrieval |
| `updateExistingBooking()` | Validate complete resource updates |
| `partialUpdateBooking()` | Validate partial updates and preserved properties |
| `deleteBooking()` | Validate resource deletion |
| `updateBookingWithoutAuthentication()` | Validate rejection and unchanged resource state |

### Engineering Experiments

| Experiment | Objective |
|---|---|
| Cleanup with `try/finally` | Attempt cleanup after test failure |
| Exception masking | Reproduce the loss of an original failure |
| Suppressed exceptions | Preserve primary and secondary failures |
| Automated teardown | Centralize resource cleanup |
| Variable shadowing | Diagnose silently skipped cleanup |
| Duplicate deletion prevention | Manage completed resource lifecycles |

Successful execution was reported after the final corrections.

The exact final aggregate test count and execution duration are not included because a consolidated final execution report was not recorded here.

---

## 18. Limitations

The laboratory intentionally maintains a focused scope.

The following limitations remain:

- Tests execute sequentially.
- The API uses a shared environment with periodic data resets.
- Read-only scenarios may depend on existing environment data.
- Resource tracking supports one registered booking per test.
- Cleanup depends on API availability and usable authentication credentials.
- A booking may become untrackable if creation succeeds but its identifier cannot be recovered.
- A resource that is successfully deleted but fails subsequent verification may trigger another cleanup attempt.
- API-level retrieval does not independently prove database-level persistence.
- Schema validation and comprehensive contract testing are not included.
- Authentication credentials are specific to the public demonstration API.

The laboratory does not claim complete environmental isolation, parallel execution safety, guaranteed cleanup under every failure condition, or comprehensive security coverage.

---

## 19. Lessons Learned

### API Testing

- HTTP status codes must be interpreted according to the API contract.
- Successful authentication does not automatically demonstrate authorization for every operation.
- PUT and PATCH require different validation strategies.
- Partial updates should verify both modified and preserved properties.
- DELETE responses should be complemented by resource state verification when deletion is under test.
- Negative authorization scenarios should verify that unauthorized side effects did not occur.

### Java Engineering

- Variable shadowing can introduce failures that remain invisible in passing test results.
- `finally` allows cleanup to be attempted after exceptions and assertion failures.
- Failures inside `finally` can replace an original failure.
- `addSuppressed()` preserves secondary diagnostic information.
- `AssertionError` belongs to the `Error` branch of Java's `Throwable` hierarchy.
- Shared mutable state requires careful lifecycle management.

### Test Automation Architecture

- API clients should execute HTTP operations without unnecessarily enforcing scenario-specific expectations.
- Tests should own the resources they create.
- Generated identifiers should be registered before additional validations can interrupt execution.
- Automated teardown should distinguish between resources that do not require cleanup and resources that cannot be cleaned successfully.
- Passing functional tests do not independently prove that setup and teardown mechanisms are correct.
- Architectural complexity should be introduced only when it solves an identified problem.

---

## References

- [REST Assured — Official Usage Guide](https://github.com/rest-assured/rest-assured/wiki/Usage)
- [Restful-Booker API Documentation](https://restful-booker.herokuapp.com/apidoc/index.html)
- [Restful-Booker — Source Code](https://github.com/mwinteringham/restful-booker)
- [TestNG — Official Documentation](https://testng.org/)
- [Jackson Databind](https://github.com/FasterXML/jackson-databind)
- [Java Language Specification — Shadowing](https://docs.oracle.com/javase/specs/jls/se21/html/jls-6.html#jls-6.4.1)
- [Java Language Specification — Execution of try-finally](https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.20.2)
- [Java Throwable — Suppressed Exceptions](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Throwable.html)
- [Java ThreadLocal Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ThreadLocal.html)
- [OWASP API Security Top 10 — 2023](https://owasp.org/API-Security/editions/2023/en/0x11-t10/)

---

## Final Conclusion

This laboratory demonstrated the evolution from reusable API test architecture toward controlled resource lifecycle management and failure-aware test automation.

The implementation combines authentication, CRUD operations, independent mutation scenarios, automated teardown, and diagnostic mechanisms.

The most valuable lessons emerged from investigating problems encountered during development: insufficient assertions, exception masking, shared state, variable shadowing, and silently skipped cleanup.

**Reliable test automation requires more than validating application behavior. It also requires validating the behavior of the automation infrastructure itself.**