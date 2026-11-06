# Lab 4 — Selenium Debugging, Dynamic DOM & Stale Elements

## Objective

Understand how Selenium handles references to dynamic DOM elements and practice diagnosing failures **before changing the test code**.

This lab deliberately reproduces three exceptions:

- `StaleElementReferenceException`
- `NoSuchElementException`
- `TimeoutException`

It then applies the findings to a small, synchronized Page Object that handles **Remove → Add** without fixed sleeps or generic retries.

## Test application

**The Internet — Dynamic Controls**  
https://the-internet.herokuapp.com/dynamic_controls

The page offers asynchronous controls for removing/adding a checkbox and enabling/disabling an input. This lab focuses on the **checkbox** scenario.

## Technologies

- Java
- Selenium WebDriver
- TestNG
- Maven
- Chrome / ChromeDriver

## Project structure

```text
src/test/java/
├── pages/
│   └── DynamicControlsPage.java
└── tests/
    ├── BaseTest.java
    └── DynamicControlsTests.java
```

## Problem: a cached `WebElement` becomes stale

First, locate and store the checkbox:

```java
WebElement oldCheckbox = driver.findElement(By.id("checkbox"));
```

Then click **Remove** and wait for the original node to become detached:

```java
dynamicControlsPage.removeCheckbox();

WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.stalenessOf(oldCheckbox));
```

Finally, try to interact with the original reference:

```java
oldCheckbox.isDisplayed(); // Intentionally throws StaleElementReferenceException
```

### Observed evidence

The experiment produced:

```text
org.openqa.selenium.StaleElementReferenceException:
stale element reference: stale element not found in the current frame
...
at org.openqa.selenium.remote.RemoteWebElement.isDisplayed(...)
at tests.DynamicControlsTests.testRemoveCheckbox(DynamicControlsTests.java:16)
```

The failure occurred in `isDisplayed()`, **not** during the original lookup or the Remove click. That evidence supports the hypothesis that the reference was valid when obtained but became stale after the checkbox node was removed.

**Important distinction:** not every DOM update makes a reference stale. Changing a different node, for example, does not necessarily invalidate this checkbox. In this experiment the referenced checkbox node itself was removed.

## Experiment 1 — `StaleElementReferenceException`

**Question:** What happens if we reuse a `WebElement` after its node has been removed?

```text
Locate checkbox (node A)
       ↓
Store WebElement A
       ↓
Click Remove
       ↓
Wait for stalenessOf(A)
       ↓
Call A.isDisplayed()
       ↓
StaleElementReferenceException
```

**Conclusion:** a `WebElement` refers to a particular element instance. It is not merely a stored `By` locator, and Selenium does not automatically rebind the old reference to a new matching element.

## Experiment 2 — `NoSuchElementException`

**Question:** What happens if we perform a *new* search after removing the checkbox?

```java
WebElement oldCheckbox = driver.findElement(By.id("checkbox"));
dynamicControlsPage.removeCheckbox();
wait.until(ExpectedConditions.stalenessOf(oldCheckbox));

driver.findElement(By.id("checkbox")); // Intentionally throws NoSuchElementException
```

**Conclusion:** this is not a stale reference. Selenium is performing a fresh lookup, but no current element matches the locator.

## Experiment 3 — `TimeoutException`

**Question:** What happens if we wait for a checkbox to reappear **without clicking Add**?

```java
WebElement oldCheckbox = driver.findElement(By.id("checkbox"));
dynamicControlsPage.removeCheckbox();

WebDriverWait removalWait =
        new WebDriverWait(driver, Duration.ofSeconds(10));
removalWait.until(ExpectedConditions.stalenessOf(oldCheckbox));

WebDriverWait appearanceWait =
        new WebDriverWait(driver, Duration.ofSeconds(3));
appearanceWait.until(
        ExpectedConditions.presenceOfElementLocated(By.id("checkbox"))
); // Intentionally throws TimeoutException
```

The first wait confirms the original reference is stale; it does **not** by itself guarantee that a loading indicator has disappeared or that a completion message is visible.

The second wait checks for the checkbox to become present again. Because **Add** was never clicked, the condition remains unsatisfied and the three-second timeout expires. The wait is working as designed; it is **not** a fixed three-second sleep.

### Exception comparison

| Exception | Operation | Meaning in this lab |
|---|---|---|
| `StaleElementReferenceException` | Use old `WebElement` | Its original DOM node is no longer attached |
| `NoSuchElementException` | Fresh `findElement()` | No current element matches the locator |
| `TimeoutException` | `wait.until(...)` | The requested condition was not satisfied within the allowed time |

## Experiment 4 — Remove → Add: same locator, new element

The complete lifecycle was tested:

```java
WebElement checkboxRemoval = driver.findElement(By.id("checkbox"));

dynamicControlsPage.removeCheckbox();

WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.stalenessOf(checkboxRemoval));

dynamicControlsPage.addCheckbox();

WebElement checkboxAddition = wait.until(
        ExpectedConditions.presenceOfElementLocated(By.id("checkbox"))
);

Assert.assertTrue(checkboxAddition.isDisplayed());
```

```text
Checkbox A — id="checkbox" — valid
             ↓ Remove
Checkbox A — stale
             ↓ Add
Checkbox B — id="checkbox" — valid
```

Although both nodes use the same `id`, they are different DOM element instances. Reusing `checkboxRemoval` after Add does not make that reference valid again.

## Refactor — Synchronization inside the Page Object

The first tests managed waits externally and used a fixed `Thread.sleep(5000)` during the initial stale reproduction. The final functional design moves the specific synchronization rules into `DynamicControlsPage`.

A focused implementation of the checkbox functionality:

```java
public class DynamicControlsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By checkbox = By.id("checkbox");
    private final By removeButton =
            By.xpath("//button[normalize-space()='Remove']");
    private final By addButton =
            By.xpath("//button[normalize-space()='Add']");

    public DynamicControlsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isCheckboxPresent() {
        return !driver.findElements(checkbox).isEmpty();
    }

    public void removeCheckbox() {
        WebElement currentCheckbox = wait.until(
                ExpectedConditions.presenceOfElementLocated(checkbox)
        );

        wait.until(
                ExpectedConditions.elementToBeClickable(removeButton)
        ).click();

        wait.until(ExpectedConditions.stalenessOf(currentCheckbox));
    }

    public void addCheckbox() {
        wait.until(
                ExpectedConditions.elementToBeClickable(addButton)
        ).click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(checkbox)
        );
    }
}
```

### Why this design?

- `By` stores **how to find** the current element; it does not hold on to a specific DOM instance.
- `removeCheckbox()` temporarily stores a `WebElement` **on purpose**, because it needs to wait for *that particular node* to become stale.
- `addCheckbox()` waits for the new checkbox to become visible rather than relying on a fixed delay.
- Both methods wait for the conditions relevant to their own operations; neither claims to wait for every possible background update.
- Tests express business behavior rather than Selenium synchronization details.

### Final functional test

```java
@Test(description = "Remove and add checkbox")
public void validateCheckboxCanBeRemovedAndAdded() {
    Assert.assertTrue(
            dynamicControlsPage.isCheckboxPresent(),
            "Checkbox should initially exist"
    );

    dynamicControlsPage.removeCheckbox();

    Assert.assertFalse(
            dynamicControlsPage.isCheckboxPresent(),
            "Checkbox should not exist after removal"
    );

    dynamicControlsPage.addCheckbox();

    Assert.assertTrue(
            dynamicControlsPage.isCheckboxPresent(),
            "Checkbox should exist after addition"
    );
}
```

> The diagnostic examples above are **intentionally failing operations**. To retain them in a green automated suite, assert the expected exception around the exact operation using TestNG's `Assert.expectThrows(...)`. Do not put a broad expected-exception annotation on an entire multi-step test; it could mask a failure at the wrong stage.

## Debugging approach

For each exception, follow the same investigation process:

1. **Observe:** record the exception type and exact failing line.
2. **Establish the previous state:** was the element located before the action?
3. **Identify the change:** which action updated the DOM?
4. **Form a hypothesis:** stale reference, fresh lookup with no match, or unmet wait condition?
5. **Reproduce in isolation:** make the failure deterministic.
6. **Fix the cause:** relocalize the new element or wait for the correct condition; avoid blind sleeps and generic retries.

## Cached `WebElement` vs `By`

| `WebElement` reference | `By` locator |
|---|---|
| Points to a particular element instance | Describes how to find an element |
| Can become stale after removal/replacement | Can be used for a new lookup |
| Useful for interacting with a known stable element or waiting for `stalenessOf` | Useful for querying the current DOM after dynamic changes |

This does **not** imply that storing `WebElement` is always incorrect. It means the expected lifecycle of the DOM node must inform the design.

This also connects to **Lab 3**, where `ProductComponent` stored a `WebElement root`. That design is reasonable while the root node remains attached, but a UI that re-renders and replaces a product card could invalidate the cached root.

## Lessons learned

- Debug from the exact failing command, not just the exception name.
- A removed/replaced node invalidates the old `WebElement` reference.
- A hidden element and a stale element are not the same state.
- The same `id` can identify a newly created node without restoring the old reference.
- A fresh lookup for a missing element differs from using a stale cached element.
- An explicit wait times out when its condition remains unmet; it is not a fixed sleep.
- `stalenessOf(oldElement)` can intentionally verify a successful removal.
- `presenceOfElementLocated` checks presence; `visibilityOfElementLocated` also requires visibility.
- Page Objects should encapsulate synchronization that belongs to the actions they expose.
- Avoid automatic global retries, broad exception catches, and arbitrary sleeps as default fixes.
- A clean Page Object design and an understanding of DOM lifecycles must work together.

## Scope

Covered: checkbox removal/recreation, exception diagnosis, explicit waits, relocalization, and Page Object synchronization.

Not covered: parallel execution, retries, automatic screenshots, reporting frameworks, iframes, Shadow DOM, or browser-specific debugging APIs.

## References

- [Selenium — Understanding Common Errors](https://www.selenium.dev/documentation/webdriver/troubleshooting/errors/)
- [Selenium Java — ExpectedConditions API](https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/ui/ExpectedConditions.html)
- [The Internet — Dynamic Controls](https://the-internet.herokuapp.com/dynamic_controls)
