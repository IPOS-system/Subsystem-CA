import api.ICatalogueAPI;
import api_impl.ICatalogueAPIService;
import domain.Item;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ICatalogueAPITest {

    static ICatalogueAPIService implRef;
    static ICatalogueAPI instance;

    @BeforeClass
    public static void setUpClass() {
        implRef = new ICatalogueAPIService();
        instance = implRef;
    }

    // listProducts should not return null
    @Test
    public void testListReturnsNonNull() {
        List<Item> products = instance.listProducts();
        assertNotNull("listProducts should not return null", products);
    }

    // The catalogue should contain at least one item
    @Test
    public void testListNotEmpty() {
        List<Item> products = instance.listProducts();
        assertFalse("listProducts should return at least one item", products.isEmpty());
    }

    // Every item in the catalogue must have a valid item ID
    @Test
    public void testListAllItemsHaveId() {
        List<Item> products = instance.listProducts();
        for (Item item : products) {
            assertNotNull("Each item must have a itemId", item.getItemId());
        }
    }

    // Every item in the catalogue must have a valid description
    @Test
    public void testListAllItemsHaveDescription() {
        List<Item> products = instance.listProducts();
        for (Item item : products) {
            assertNotNull("Each item must have a description", item.getDescription());
        }
    }

    // Paracetamol (ID 10000001) should appear in the catalogue
    @Test
    public void testListContainsKnownItem() {
        List<Item> products = instance.listProducts();
        boolean found = false;
        for (Item item : products) {
            if ("10000001".equals(item.getItemId())) {
                found = true;
                break;
            }
        }
        assertTrue("Catalogue should contain the item with ID 10000001", found);
    }

    // The specified item should have the expected description
    @Test
    public void testListKnownItemDescription() {
        List<Item> products = instance.listProducts();
        for (Item item : products) {
            if ("10000001".equals(item.getItemId())) {
                assertEquals("Paracetamol", item.getDescription());
                return;
            }
        }
        fail("Item 10000001 not found in catalogue");
    }
}