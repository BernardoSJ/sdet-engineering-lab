package tests;

import components.ProductComponent;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.InventoryPage;

import java.util.List;
import java.util.Optional;

public class InventoryTests extends BaseTest {

    @Test(description = "Verify the user can log in")
    public void testUserCanLogIn() {
        InventoryPage inventoryInfo = loginPage.logInWithValidCredentials("standard_user", "secret_sauce");
        boolean isLogged = inventoryInfo.isUserLogged();
        int countProducts = inventoryInfo.getNumberOfProducts();
        Assert.assertTrue(isLogged);
        Assert.assertEquals(countProducts, 6);
    }

    @Test(description = "Verify the number of products is 6")
    public void verifyProductInformation() {

        InventoryPage inventoryPage = loginPage.logInWithValidCredentials("standard_user", "secret_sauce");

        List<ProductComponent> products = inventoryPage.getProducts();

        Assert.assertEquals(products.size(), 6);

        for (ProductComponent product : products) {
            System.out.println("Name: " + product.getName());
            System.out.println("Description: " + product.getDescription());
            System.out.println("Price: " + product.getPrice());
        }
    }

    @Test(description = "Search for a product by name")
    public void searchProductByName() {
        Optional<ProductComponent> product = loginPage.logInWithValidCredentials("standard_user", "secret_sauce")
                .getProductByName("Sauce Labs Backpack");

        Assert.assertTrue(product.isPresent(), "Product not found");
        Assert.assertEquals(product.get().getName(), "Sauce Labs Backpack");

    }
}
