# Selenium Parallel Execution Lab

## Objective
Demonstrate the use of WebDriver for parallel executions, to avoid conflicts between tests, miss information handling.

## Initial Architecture
```text
src/
  ├── test
       ├── java/           
            ├── pages   # Page Object Models
            ├── tests   # UI tests
  ├── pom.xml           # Maven configuration file
  └── testng.xml         # Test execution file
```

## Problem

## Hypothesis

## Evidence

## Root Cause
The tests failed because the WebDriver reference was declared as static, causing all parallel test threads to share and overwrite the same reference. Although every @BeforeMethod initially created a separate browser session, subsequent threads replaced the shared driver reference. As a result, tests and teardown methods could interact with WebDriver sessions created by other threads, including closing sessions that were still in use.

## Solution

## Why ThreadLocal?

## Why ThreadGuard?

## Final Architecture

## Lessons Learned