# Lab 3 - Java + Selenium Design

## Objective

The objective of this lab is to improve the design of a Selenium framework using Java OOP concepts.

The main topics explored are:

- Page Object Model
- Page Component Objects
- Encapsulation
- Composition
- Inheritance
- `HAS-A` vs `IS-A`
- Collections
- Streams
- `Optional`
- Page transitions
- Responsibility separation

The goal is not only to automate an application, but to model it using objects with clear responsibilities.

---

## Test Application

This lab uses:

```text
https://www.saucedemo.com/
```

The main flow covered is:

```text
Login
  ↓
Inventory
  ↓
Search product
  ↓
Add product to cart
  ↓
Verify cart count
  ↓
Open cart
  ↓
Verify product
```

---

## Initial Design

The first version used basic Page Objects:

```text
LoginPage
InventoryPage
CartPage
```

At this stage, `InventoryPage` could locate the inventory items and count the number of products.

Example:

```java
public int getNumberOfProducts() {
    return driver.findElements(inventoryElements).size();
}
```

This worked, but the inventory contained multiple repeated elements with the same structure.

Each product included:

```text
name
description
price
add to cart button
```

This suggested that each product should be represented as an independent Java object.

---

## Product Component

A `ProductComponent` was created to represent each inventory item.

The root element of the product is passed to the component:

```java
public class ProductComponent {

    private final WebElement root;

    public ProductComponent(WebElement root) {
        this.root = root;
    }
}
```

Each component searches only inside its own root element.

Example:

```java
public String getName() {
    return root.findElement(name).getText();
}
```

This creates the following relationship:

```text
InventoryPage
    │
    └── HAS-MANY ProductComponent
```

Instead of managing independent lists such as:

```text
product names
product prices
product buttons
```

each product becomes a single object.

This avoids relying on matching indexes between different collections.

---

## Composition

The inventory page contains multiple product components:

```java
private final List<ProductComponent> products =
        new ArrayList<>();
```

The products are created from the inventory elements:

```java
List<WebElement> elements =
        driver.findElements(inventoryElements);

for (WebElement element : elements) {
    products.add(new ProductComponent(element));
}
```

Conceptually:

```text
WebElement
     ↓
ProductComponent

WebElement
     ↓
ProductComponent

WebElement
     ↓
ProductComponent
```

This is an example of composition.

```text
InventoryPage HAS-MANY ProductComponent
```

The inventory page manages the collection, while each product manages its own data and behavior.

---

## Encapsulation

The test does not need to know how a product is located internally.

Instead of:

```java
driver.findElement(...)
```

the test can use:

```java
product.getName();
product.getPrice();
product.addToCart();
```

The implementation details remain inside the component.

The test only interacts with behavior exposed by the object.

---

## Searching Products

The first implementation used a traditional `for` loop:

```java
public ProductComponent getProductByName(
        String productName) {

    for (ProductComponent product : products) {

        if (product.getName().equals(productName)) {
            return product;
        }
    }

    return null;
}
```

This implementation worked, but returning `null` introduced the possibility of `NullPointerException`.

---

## Optional

The return type was changed to:

```java
Optional<ProductComponent>
```

This makes the possibility of not finding a product explicit in the method contract.

Example:

```java
public Optional<ProductComponent> getProductByName(
        String productName) {

    return products.stream()
            .filter(product ->
                    product.getName().equals(productName))
            .findFirst();
}
```

The test can decide how to handle the absence of the product:

```java
ProductComponent backpack =
        inventoryPage
                .getProductByName(
                        "Sauce Labs Backpack")
                .orElseThrow(() ->
                        new AssertionError(
                                "Product not found"));
```

---

## Streams

The product search was refactored from an imperative loop:

```text
for
  ↓
compare each product
  ↓
return match
```

to a declarative stream:

```text
products
   ↓
stream()
   ↓
filter()
   ↓
findFirst()
   ↓
Optional<ProductComponent>
```

The purpose of using Streams was not simply to reduce the number of lines.

The objective was to understand how Java Collections can be queried using declarative operations.

---

## Header Component

The shopping cart link and cart badge do not belong specifically to the inventory logic.

They represent a different region of the UI.

For this reason, a `HeaderComponent` was introduced.

```java
public class HeaderComponent {

    private final WebDriver driver;

    private final By cartLink =
            By.className("shopping_cart_link");

    private final By cartBadge =
            By.className("shopping_cart_badge");

    public HeaderComponent(WebDriver driver) {
        this.driver = driver;
    }
}
```

`InventoryPage` contains the component:

```java
private final HeaderComponent header;
```

and initializes it using:

```java
this.header = new HeaderComponent(driver);
```

The relationship becomes:

```text
InventoryPage HAS-A HeaderComponent
```

This allows the header logic to remain independent from the inventory logic.

---

## Cart Item Count

The cart badge does not exist when the cart is empty.

Instead of using an exception as normal control flow, the implementation checks whether the badge exists.

Conceptually:

```text
badge exists
    ↓
read value
    ↓
return count

badge does not exist
    ↓
return 0
```

This provides a simple contract:

```text
getCartItemCount()

0 → empty cart
1 → one product
2 → two products
...
```

---

## Page Transitions

Page Objects and Components return the next page when an action causes navigation.

Example:

```java
public InventoryPage logInWithValidCredentials(
        String username,
        String password) {

    driver.findElement(usernameField)
            .sendKeys(username);

    driver.findElement(passwordField)
            .sendKeys(password);

    driver.findElement(loginButton)
            .click();

    return new InventoryPage(driver);
}
```

The transition is:

```text
LoginPage
    ↓
login
    ↓
InventoryPage
```

The header also models navigation:

```java
public CartPage openCart() {
    driver.findElement(cartLink).click();
    return new CartPage(driver);
}
```

The transition becomes:

```text
HeaderComponent
     ↓
openCart()
     ↓
CartPage
```

---

## Cart Page

The cart page verifies whether a product exists in the cart.

Instead of using `findElement()`, all matching product names are evaluated:

```java
public boolean containsProduct(String productName) {

    return driver.findElements(inventoryItem)
            .stream()
            .anyMatch(element ->
                    element.getText()
                            .equals(productName));
}
```

This supports:

```text
empty cart
one product
multiple products
```

The method returns:

```text
true  → product exists
false → product does not exist
```

---

# Inheritance

A small `BasePage` was introduced:

```java
public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
    }
}
```

The application pages extend it:

```java
public class LoginPage extends BasePage
```

```java
public class InventoryPage extends BasePage
```

```java
public class CartPage extends BasePage
```

This creates an `IS-A` relationship.

```text
LoginPage IS-A BasePage
InventoryPage IS-A BasePage
CartPage IS-A BasePage
```

The subclasses initialize the parent class using:

```java
super(driver);
```

This removes duplicated `WebDriver` fields from each Page Object.

---

## Composition vs Inheritance

The project uses both concepts for different purposes.

### Inheritance

Inheritance represents an `IS-A` relationship.

```text
InventoryPage IS-A BasePage
```

Example:

```java
public class InventoryPage extends BasePage
```

### Composition

Composition represents a `HAS-A` relationship.

```text
InventoryPage HAS-A HeaderComponent
InventoryPage HAS-MANY ProductComponent
```

Example:

```java
private final HeaderComponent header;
```

and:

```java
private final List<ProductComponent> products;
```

---

## Why Components Do Not Extend BasePage

It would technically be possible to write:

```java
public class HeaderComponent extends BasePage
```

to reuse the `WebDriver`.

However, this would represent:

```text
HeaderComponent IS-A Page
```

which is semantically incorrect.

A header is not a page.

A header belongs to a page.

Therefore:

```text
Page HAS-A HeaderComponent
```

is a more appropriate relationship.

Code reuse alone is not enough reason to use inheritance.

---

## Avoiding a Large BasePage

`BasePage` was intentionally kept small.

It currently contains only:

```java
protected final WebDriver driver;
```

Methods such as:

```text
click()
sendKeys()
waitForElement()
scroll()
JavaScript click()
takeScreenshot()
```

were not automatically added to `BasePage`.

Adding every reusable Selenium operation to the parent class could eventually create a large class with too many responsibilities.

The framework instead favors domain-oriented methods such as:

```java
loginAs()
addToCart()
openCart()
containsProduct()
```

These methods describe application behavior instead of low-level Selenium operations.

---

## Final Architecture

```text
                     BasePage
                 /      |       \
                /       |        \
         LoginPage InventoryPage CartPage
                       │
                 ┌─────┴─────┐
                 │           │
               HAS-A       HAS-MANY
                 │           │
                 ▼           ▼
        HeaderComponent ProductComponent
```

Application flow:

```text
LoginPage
    │
    │ login
    ▼
InventoryPage
    │
    ├── ProductComponent
    │       │
    │       └── addToCart()
    │
    └── HeaderComponent
            │
            └── openCart()
                  │
                  ▼
               CartPage
```

---

## Example Test

```java
@Test(
    description =
        "Add a product to the cart, verify the cart item count " +
        "and check if the product is present in the cart"
)
public void addProductToCartAndVerifyCount() {

    InventoryPage inventoryPage =
            loginPage
                    .logInWithValidCredentials(
                            "standard_user",
                            "secret_sauce"
                    );

    ProductComponent backpack =
            inventoryPage
                    .getProductByName(
                            "Sauce Labs Backpack"
                    )
                    .orElseThrow(() ->
                            new AssertionError(
                                    "Product not found"
                            )
                    );

    backpack.addToCart();

    Assert.assertEquals(
            inventoryPage
                    .getHeader()
                    .getCartItemCount(),
            1,
            "Cart item count is incorrect"
    );

    CartPage cartPage =
            inventoryPage
                    .getHeader()
                    .openCart();

    Assert.assertTrue(
            cartPage.containsProduct(
                    "Sauce Labs Backpack"
            ),
            "Product was not found in the cart"
    );
}
```

The test does not directly use:

```text
WebDriver
WebElement
By
XPath
CSS Selector
```

It interacts with application concepts instead.

---

# Lessons Learned

- Page Objects should expose application behavior rather than Selenium implementation details.
- Repeated UI structures can be modeled as Page Components.
- A `WebElement root` can define the scope of a component.
- Components avoid relying on matching indexes between multiple independent lists.
- `List<ProductComponent>` is more expressive than managing product attributes separately.
- Streams can simplify collection queries using operations such as `filter()`, `findFirst()` and `anyMatch()`.
- `Optional` explicitly represents the possibility of a missing value.
- `Optional` does not require Streams and can also be used with traditional loops.
- Returning `null` can introduce accidental `NullPointerException`.
- Assertions should remain in tests instead of Page Objects or Components.
- Page transitions can be represented through method return types.
- Composition models `HAS-A` relationships.
- Inheritance models `IS-A` relationships.
- Code reuse alone is not sufficient justification for inheritance.
- Components should not extend `BasePage` simply to reuse `WebDriver`.
- A small `BasePage` is preferable to a large parent class containing unrelated utilities.
- Abstractions should solve an existing design problem instead of anticipating every possible future requirement.

---

## Final Result

The project evolved from basic Page Objects:

```text
LoginPage
InventoryPage
CartPage
```

into a model based on responsibilities and object relationships:

```text
Page Objects
    ↓
ProductComponent
    ↓
HAS-MANY composition
    ↓
Collections
    ↓
Streams
    ↓
Optional
    ↓
HeaderComponent
    ↓
HAS-A composition
    ↓
BasePage
    ↓
IS-A inheritance
```

The final framework models application concepts instead of exposing Selenium implementation details directly to the tests.