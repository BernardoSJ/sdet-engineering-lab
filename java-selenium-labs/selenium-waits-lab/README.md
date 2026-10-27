# Selenium Waits & Synchronization Lab

## Objective

Explore different synchronization strategies in Selenium and understand when each one should be used.

The main goal of this lab is to compare:

- `Thread.sleep()`
- Implicit Wait
- Explicit Wait with `WebDriverWait`
- `ExpectedConditions`
- `FluentWait`

The lab also demonstrates the difference between waiting for an element to be **present in the DOM** and waiting for an element to become **visible to the user**.

---

## Test Application

This lab uses Selenium's dynamic test page:

```text
https://www.selenium.dev/selenium/web/dynamic.html
```

The page provides two useful dynamic scenarios:

1. **Add a box**
   - The element does not initially exist in the DOM.
   - It is added after clicking the button.

2. **Reveal an input**
   - The input already exists in the DOM.
   - It is initially hidden and becomes visible after clicking the reveal button.

These two behaviors make it possible to demonstrate different synchronization problems.

---

## Project Structure

```text
selenium-waits-lab/
│
├── pom.xml
├── testng.xml
│
└── src/test/java/
    ├── pages/
    │   └── DynamicPage.java
    │
    ├── tests/
    │   ├── BaseTest.java
    │   └── WaitsTest.java
    │
    └── utils/
        └── WaitUtility.java
```

---

## Initial Problem

The first implementation did not use any wait strategy.

After clicking **Add a box**, the test immediately attempted to locate:

```java
By.id("box0")
```

Because the element had not yet been added to the DOM, Selenium produced:

```text
NoSuchElementException
```

The second scenario behaved differently.

After clicking **Reveal a new input**, Selenium could locate:

```java
By.id("revealed")
```

because the element already existed in the DOM.

However:

```java
element.isDisplayed()
```

returned:

```text
false
```

because the element had not yet become visible.

This demonstrated an important difference:

```text
Element not present
        ↓
findElement() fails
        ↓
NoSuchElementException
```

versus:

```text
Element present but hidden
        ↓
findElement() succeeds
        ↓
isDisplayed() returns false
```

---

## Experiment 1 — Thread.sleep()

A fixed wait was initially added:

```java
Thread.sleep(2000);
```

This allowed the application more time to update before the validation was executed.

The tests could pass, but the solution introduced two problems.

### Element becomes ready before the sleep finishes

```text
Element ready after 500 ms
Thread.sleep = 2000 ms

→ approximately 1500 ms wasted
```

### Element becomes ready after the sleep finishes

```text
Element ready after 2500 ms
Thread.sleep = 2000 ms

→ test can fail
```

Increasing the sleep duration would only move the problem and increase execution time and maintenance cost.

### Conclusion

`Thread.sleep()` waits for a fixed amount of time regardless of the real state of the application.

It does not know what condition the test is waiting for.

---

## Experiment 2 — Implicit Wait

An implicit wait was then configured:

```java
driver.manage()
      .timeouts()
      .implicitlyWait(Duration.ofSeconds(2));
```

### Add Box

The implicit wait helped the `box0` scenario because Selenium was trying to locate an element that did not yet exist.

Instead of immediately throwing `NoSuchElementException`, Selenium had time to retry the lookup.

```text
findElement(box0)
        ↓
element not found
        ↓
wait / retry
        ↓
element appears
        ↓
continue test
```

### Reveal Input

The implicit wait did not solve the revealed input scenario.

The reason is that the input already existed in the DOM.

```text
findElement(revealed)
        ↓
element found immediately
        ↓
implicit wait finishes
        ↓
isDisplayed()
        ↓
false
```

### Conclusion

Implicit wait mainly affects element location.

It does not allow the test to express a specific state such as:

```text
wait until visible
wait until clickable
wait until text appears
```

---

## Experiment 3 — Explicit Wait

The implicit wait was removed and replaced with explicit waits using:

```java
WebDriverWait
```

and:

```java
ExpectedConditions
```

Two different conditions were used because the elements had different synchronization requirements.

### Presence

For the dynamically created box:

```java
ExpectedConditions.presenceOfElementLocated(addedBox)
```

The requirement is:

```text
Wait until box0 exists in the DOM.
```

### Visibility

For the hidden input:

```java
ExpectedConditions.visibilityOfElementLocated(revealedInput)
```

The requirement is:

```text
Wait until the input exists and becomes visible.
```

This solved both synchronization problems without using fixed delays.

---

## Reusable Wait Utility

Common explicit waits were extracted into a small utility class.

```java
public class WaitUtility {

    private static final Duration DEFAULT_TIMEOUT =
            Duration.ofSeconds(2);

    private final WebDriverWait wait;

    public WaitUtility(WebDriver driver) {
        this.wait =
                new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    public WebElement waitForVisibility(By locator) {
        return wait.until(
                ExpectedConditions
                        .visibilityOfElementLocated(locator)
        );
    }

    public WebElement waitForPresence(By locator) {
        return wait.until(
                ExpectedConditions
                        .presenceOfElementLocated(locator)
        );
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(
                ExpectedConditions
                        .elementToBeClickable(locator)
        );
    }
}
```

The purpose of this class is to keep generic and reusable synchronization logic outside individual Page Objects.

---

## Experiment 4 — FluentWait

A `FluentWait` was added to understand how polling works internally.

```java
Wait<WebDriver> fluentWait =
        new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(5))
                .pollingEvery(Duration.ofMillis(200))
                .ignoring(NoSuchElementException.class);
```

The condition was evaluated repeatedly:

```java
return fluentWait.until(driver -> {
    System.out.println(
            "Checking box at: "
                    + System.currentTimeMillis()
    );

    return driver
            .findElement(addedBox)
            .isDisplayed();
});
```

Conceptually:

```text
Maximum timeout: 5 seconds
Polling interval: 200 ms

Check element
     ↓
not found
     ↓
NoSuchElementException ignored
     ↓
wait approximately 200 ms
     ↓
check again
     ↓
...
     ↓
element found
     ↓
condition true
     ↓
continue test
```

The full timeout is not always consumed.

If the condition becomes true before the timeout, execution continues immediately.

---

## Timeout vs Polling

### Timeout

Defines the maximum amount of time the wait can continue before failing.

Example:

```text
Timeout = 5 seconds
```

does not mean:

```text
Always wait 5 seconds
```

It means:

```text
Wait up to 5 seconds.
```

### Polling

Defines approximately how frequently the condition is evaluated.

Example:

```text
Polling = 200 ms
```

means the wait periodically checks whether the condition has become true.

---

## Ignored Exceptions

During a `FluentWait`, some exceptions can represent an expected temporary state.

For example:

```text
NoSuchElementException
```

may simply mean that a dynamically generated element has not appeared yet.

Ignoring the exception during the wait does not permanently hide the problem.

If the condition is never satisfied before the timeout, the wait still fails.

---

## Final DynamicPage Design

Generic synchronization is delegated to `WaitUtility`.

```java
public boolean isAddedBoxPresent() {
    return waitUtility.waitForPresence(addedBox) != null;
}

public boolean isRevealedInputVisible() {
    return waitUtility.waitForVisibility(revealedInput) != null;
}
```

A more specific synchronization requirement can remain inside the Page Object:

```java
public boolean isAddedBoxPresentWithFluentWait() {

    Wait<WebDriver> fluentWait =
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(5))
                    .pollingEvery(Duration.ofMillis(200))
                    .ignoring(NoSuchElementException.class);

    return fluentWait.until(
            currentDriver ->
                    currentDriver
                            .findElement(addedBox)
                            .isDisplayed()
    );
}
```

---

## Synchronization Strategy

The main design rule used in this lab is:

```text
Generic and reusable wait behavior
        ↓
WaitUtility
```

while:

```text
Synchronization specific to a page
or a special application behavior
        ↓
Page Object / Component
```

Not every wait needs to be extracted into a utility.

An abstraction should be introduced when it provides real reuse or improves maintainability.

---

## Lessons Learned

- `Thread.sleep()` waits for time, not application state.
- A fixed sleep can make tests slower and still fail when the application takes longer than expected.
- Implicit waits mainly affect element lookup.
- An element being present in the DOM does not mean that it is visible.
- `presenceOfElementLocated()` and `visibilityOfElementLocated()` solve different synchronization problems.
- Explicit waits allow tests to wait for a specific application state instead of an arbitrary amount of time.
- A timeout represents the maximum waiting time, not a mandatory delay.
- Polling defines how frequently a wait reevaluates its condition.
- Temporary exceptions such as `NoSuchElementException` can be ignored during a controlled polling period.
- `FluentWait` provides more control over timeout, polling, and ignored exceptions.
- Generic waits can be extracted into reusable utilities, while page-specific synchronization can remain inside Page Objects.
- Adding waits everywhere is not a good synchronization strategy; waits should address an actual application behavior.
- A `NoSuchElementException` does not automatically mean that the timeout needs to be increased. The page state, locator, navigation, and synchronization requirement should be investigated first.

---

## Final Result

The final implementation uses synchronization based on application state instead of fixed delays.

```text
Thread.sleep()
        ↓
fixed time

Implicit Wait
        ↓
element lookup

Explicit Wait
        ↓
specific expected state

FluentWait
        ↓
specific state
+ timeout control
+ polling control
+ ignored exceptions
```

The tests execute consistently without depending on arbitrary fixed delays.