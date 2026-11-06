# SDET Engineering Labs

**Hands-on experiments focused on software test automation, engineering practices, and continuous technical improvement.**

## About This Repository

This repository documents my ongoing learning journey as a Software Development Engineer in Test (SDET), with a focus on strengthening my technical skills through practical experimentation.

The main objective is not simply to build automated tests or explore different tools, but to gain a deeper understanding of the engineering principles behind reliable, maintainable, and scalable test automation.

Instead of building one large framework from the beginning, I decided to work on smaller, focused laboratories. Each lab addresses a specific technical challenge, allowing me to investigate its behavior, reproduce issues, evaluate solutions, and document the lessons learned.

**My approach follows five steps:**

1. **Reproduce:** Create or identify a specific technical problem.
2. **Investigate:** Observe the behavior, collect evidence, and analyze possible causes.
3. **Understand:** Identify the root cause and the engineering concepts involved.
4. **Improve:** Implement a solution and evaluate its trade-offs.
5. **Document:** Record the results, decisions, limitations, and lessons learned.

The goal is to develop a stronger engineering mindset rather than relying only on tool-specific knowledge.

---

## Completed Labs — Java & Selenium

The first phase focuses on Java and Selenium WebDriver, exploring four important areas of test automation engineering.

| Lab | Focus Area | Key Topics | Status |
|---|---|---|---|
| **01 — Parallel Execution** | Concurrency and test isolation | TestNG, ThreadLocal, ThreadGuard, shared state, WebDriver lifecycle | Completed |
| **02 — Synchronization** | Reliable interaction with dynamic UI | Implicit Wait, Explicit Wait, FluentWait, ExpectedConditions, polling | Completed |
| **03 — Framework Design** | Object-oriented design | Page Object Model, Page Components, encapsulation, composition, inheritance, Streams, Optional | Completed |
| **04 — Debugging & Dynamic DOM** | Root cause analysis | StaleElementReferenceException, NoSuchElementException, TimeoutException, element references, DOM changes | Completed |

### Lab Details

**Lab 01 — Parallel Execution & Thread Safety**

Investigates the problems caused by sharing WebDriver instances across parallel test executions.

The lab reproduces concurrency-related failures and explores thread isolation using `ThreadLocal`, along with `ThreadGuard` for detecting cross-thread WebDriver access.

[Explore Lab 01](./java-selenium-labs/selenium-parallel-lab/)

**Lab 02 — Waits & Synchronization**

Explores synchronization challenges in dynamic web applications.

The lab compares fixed delays, implicit waits, explicit waits, and FluentWait, emphasizing the importance of waiting for application conditions instead of relying on arbitrary delays.

[Explore Lab 02](./java-selenium-labs/selenium-waits-lab/)

**Lab 03 — Page Objects, Components & OOP**

Focuses on improving test automation design through object-oriented principles.

The lab explores component-based modeling, separation of responsibilities, encapsulation, composition (`HAS-A`), inheritance (`IS-A`), and Java Collections and Streams.

[Explore Lab 03](./java-selenium-labs/selenium-design-lab/)

**Lab 04 — Debugging & Dynamic DOM**

Investigates how Selenium handles references to elements that are removed or replaced in the DOM.

The lab deliberately reproduces different WebDriver exceptions, analyzes their causes, and explores how to manage element references and synchronization more reliably.

[Explore Lab 04](./java-selenium-labs/selenium-debugging-lab/)

---

## Technologies Used

**Language:** Java

**Browser Automation:** Selenium WebDriver

**Testing Framework:** TestNG

**Build & Dependency Management:** Maven

**Additional Concepts:** Object-Oriented Programming, Concurrency, Collections, Streams, Synchronization, and Debugging

Each laboratory includes its own documentation explaining the implementation, experiments, technical decisions, and findings.

---

## Key Engineering Takeaways

Throughout these laboratories, I focused on several principles that extend beyond Selenium:

- **Test isolation matters:** Shared mutable state can introduce unpredictable failures during parallel execution.
- **Synchronization must be intentional:** Waiting for application state is more reliable than using arbitrary delays.
- **Good design reflects responsibilities:** Page Objects and Components should expose application behavior while hiding implementation details.
- **Abstractions should solve real problems:** More classes, patterns, or utilities do not automatically result in better architecture.
- **Debugging requires evidence:** Understanding the exact failure and its root cause is more valuable than applying generic fixes.
- **Technical decisions involve trade-offs:** Different approaches may be valid depending on application behavior and maintenance requirements.

These experiments reinforced the importance of understanding not only *how* an implementation works, but also *why* a particular approach is appropriate.

---

## What's Next?

The Java + Selenium phase is complete, but this repository is part of a broader learning initiative.

My next areas of focus are:

### 1. API Testing & Engineering — Java + RestAssured

The next phase will explore API automation beyond basic request and response validation.

Planned topics include:

- API test architecture and reusable request specifications
- Serialization and deserialization
- Positive and negative testing
- Authentication and authorization scenarios
- API contracts and schema validation
- Data consistency and idempotency
- Error handling and debugging

The intention is to apply the same investigation-driven approach used in the Selenium laboratories.

### 2. Playwright & TypeScript — Advanced Automation Practices

Another area of focus will be strengthening automation engineering with Playwright and TypeScript.

Potential topics include:

- Framework architecture and maintainability
- Fixtures and test isolation
- Reliable locators and synchronization
- Dynamic test data
- API and UI integration
- Debugging and failure analysis

### 3. AI-Assisted Testing & Engineering

I also plan to continue exploring how AI-assisted tools can support software testing activities, including test development, analysis, troubleshooting, and maintainability.

The focus will be on evaluating practical applications, understanding limitations, and keeping engineering judgment central to the process.

*These are planned learning areas, not completed implementations. Their scope will evolve as I progress.*

---

## Feedback & Collaboration

I am sharing these laboratories as part of my continuous professional development.

The implementations reflect my current understanding and the technical decisions made during each experiment. They are not intended to be universal solutions or production-ready frameworks.

I'm open to constructive feedback, alternative approaches, code reviews, and suggestions for improvement.

If you notice something that could be designed differently, implemented more effectively, or investigated further, feel free to share your perspective.

**The purpose of this repository is not to demonstrate that I know everything, but to document how I approach problems, learn from them, and continuously improve as an engineer.**

---

## References

- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [TestNG Documentation](https://testng.org/)
- [Java Documentation](https://docs.oracle.com/en/java/)
- [Maven Documentation](https://maven.apache.org/)

---

**Continuous learning through experimentation, critical thinking, and engineering practice.**