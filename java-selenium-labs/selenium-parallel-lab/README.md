# Selenium Parallel Execution Lab

## Objective

Demonstrate the problems caused by sharing a static `WebDriver` reference during parallel TestNG execution, identify the root cause through execution logs, and implement thread-safe WebDriver management using `ThreadLocal` and `ThreadGuard`.

## Initial Architecture

```text
selenium-parallel-lab/
├── src/
│   └── test/
│       └── java/
│           ├── pages/      # Page Object Models
│           └── tests/      # UI tests
├── pom.xml                 # Maven configuration
└── testng.xml              # TestNG suite configuration
```

## Problem

The test suite worked correctly during sequential execution.

However, after enabling parallel execution with TestNG, the tests started failing with inconsistent and unpredictable errors such as:

- `NoSuchSessionException`
- `NoSuchElementException`
- Incorrect assertion results
- WebDriver sessions being closed while other tests were still using them

The initial implementation used a shared static WebDriver:

```java
protected static WebDriver driver;
```

This allowed multiple execution threads to overwrite and access the same WebDriver reference.

## Hypothesis

The failures were caused by multiple parallel threads sharing the same static `WebDriver` reference.

Although each `@BeforeMethod` created a new ChromeDriver instance, every new assignment replaced the shared reference.

As a result, one thread could access or terminate a WebDriver session created or currently being used by another thread.

## Evidence

During parallel execution, each thread initially created a different WebDriver:

```text
[DRIVER CREATED] Test: invalidLogin
Thread: 30
Driver Identity: 1890173883

[DRIVER CREATED] Test: verifyNumberOfProducts
Thread: 34
Driver Identity: 462085002

[DRIVER CREATED] Test: validLogin
Thread: 32
Driver Identity: 1367086561

[DRIVER CREATED] Test: verifyLogout
Thread: 33
Driver Identity: 394171398

[DRIVER CREATED] Test: lockedUserLogin
Thread: 31
Driver Identity: 356509382
```

However, during teardown, different threads accessed the same WebDriver reference:

```text
[TEARDOWN] Test: validLogin
Thread: 32
Driver Identity: 356509382

[TEARDOWN] Test: verifyNumberOfProducts
Thread: 34
Driver Identity: 356509382
```

This demonstrated that the static WebDriver reference was being overwritten between execution threads.

The suite subsequently produced failures such as:

```text
NoSuchSessionException:
Session ID is null.
Using WebDriver after calling quit()?
```

and:

```text
NoSuchElementException
```

After implementing isolated WebDriver management, each thread created and closed the same driver instance assigned to it:

```text
Thread 33
Created Driver: 1626670185
Teardown Driver: 1626670185

Thread 31
Created Driver: 1822740791
Teardown Driver: 1822740791

Thread 43
Created Driver: 87593261
Teardown Driver: 87593261
```

The suite was executed repeatedly with the final result:

```text
Tests run: 5
Failures: 0
Errors: 0
Skipped: 0
```

## Root Cause

The tests failed because the WebDriver reference was declared as `static`, causing all parallel execution threads to share and overwrite the same reference.

Although every `@BeforeMethod` initially created a separate browser session, subsequent threads replaced the shared WebDriver reference.

As a result, test methods and teardown methods could interact with sessions belonging to other execution threads, including closing browser sessions that were still in use.

## Solution

A thread-safe WebDriver management strategy was implemented using `ThreadLocal<WebDriver>`.

```java
private static final ThreadLocal<WebDriver> driver =
        new ThreadLocal<>();
```

Each execution thread stores and retrieves its own WebDriver reference:

```java
public static WebDriver getDriver() {
    return driver.get();
}

public static void setDriver(WebDriver driverInstance) {
    driver.set(driverInstance);
}
```

The driver is cleaned up after each test:

```java
public static void removeDriver() {
    WebDriver currentDriver = driver.get();

    if (currentDriver != null) {
        try {
            currentDriver.quit();
        } finally {
            driver.remove();
        }
    }
}
```

`ThreadGuard` was also added when creating the WebDriver:

```java
WebDriver driver =
        ThreadGuard.protect(new ChromeDriver());
```

## Why ThreadLocal?

`ThreadLocal` allows every execution thread to maintain its own independent WebDriver reference.

Instead of multiple threads sharing:

```text
Thread A ─┐
Thread B ─┼── Shared WebDriver
Thread C ─┘
```

the framework now behaves as:

```text
Thread A ─── WebDriver A
Thread B ─── WebDriver B
Thread C ─── WebDriver C
```

This prevents threads from overwriting each other's WebDriver references during parallel execution.

## Why ThreadGuard?

`ThreadGuard` provides an additional validation layer.

It detects when a WebDriver created by one thread is accessed from another thread and throws an exception instead of allowing unsafe cross-thread WebDriver usage.

`ThreadGuard` complements `ThreadLocal`; it does not replace it.

## Why remove()?

`quit()` and `remove()` have different responsibilities.

```text
quit()
└── Terminates the WebDriver/browser session.

remove()
└── Removes the WebDriver value associated with the current thread.
```

Calling `remove()` is important because execution frameworks can reuse threads.

Leaving values stored in a `ThreadLocal` can allow stale state to remain associated with reused threads and can retain objects longer than necessary.

Using `finally` ensures that the `ThreadLocal` reference is removed even if `quit()` fails.

## Final Architecture

```text
selenium-parallel-lab/
├── src/
│   └── test/
│       └── java/
│           ├── driver/
│           │   └── DriverFactory.java
│           ├── pages/
│           │   ├── LoginPage.java
│           │   └── InventoryPage.java
│           └── tests/
│               ├── BaseTest.java
│               └── LoginTests.java
├── pom.xml
└── testng.xml
```

The final execution flow is:

```text
TestNG parallel execution
        │
        ├── Thread A
        │      └── ThreadLocal
        │             └── WebDriver A
        │
        ├── Thread B
        │      └── ThreadLocal
        │             └── WebDriver B
        │
        └── Thread C
               └── ThreadLocal
                      └── WebDriver C
```

## Lessons Learned

- Parallel execution requires test state to be isolated between execution threads.
- A static mutable WebDriver reference is unsafe when multiple test threads can overwrite it.
- TestNG can execute test methods in parallel using `parallel="methods"` and `thread-count`.
- Maven Surefire must be configured to execute the intended TestNG suite configuration when using `testng.xml`.
- `ThreadLocal` provides an independent WebDriver reference for each execution thread.
- `ThreadGuard` detects incorrect cross-thread WebDriver access and acts as an additional protection layer.
- `quit()` closes the browser session, while `ThreadLocal.remove()` cleans the value associated with the current thread.
- Successful execution alone is not enough to prove thread safety; thread IDs and WebDriver identities provide useful evidence about the real execution behavior.
- Shared mutable state should be minimized when designing frameworks intended for parallel execution.