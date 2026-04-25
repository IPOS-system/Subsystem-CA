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

    // listProducts should not return null and contain at least one item
    @Test
    public void testListNotEmpty() {
        List<Item> products = instance.listProducts();
        assertNotNull("listProducts should not return null", products);
        assertFalse("listProducts should return at least one item", products.isEmpty());
    }

    // Every item in the catalogue must have a valid item ID
    @Test
    public void testAllItemsHaveId() {
        List<Item> products = instance.listProducts();
        for (Item item : products) {
            assertNotNull("Each item must have a itemId", item.getItemId());
        }
    }

    // Every item in the catalogue must have a valid description (name)
    @Test
    public void testAllItemsHaveDescription() {
        List<Item> products = instance.listProducts();
        for (Item item : products) {
            assertNotNull("Each item must have a description (name)", item.getDescription());
        }
    }

    // Paracetamol (ID 10000001) should appear in the catalogue
    @Test
    public void testListContainsItem() {
        String id = "10000001";
        List<Item> products = instance.listProducts();
        boolean found = false;
        for (Item item : products) {
            if (id.equals(item.getItemId())) {
                found = true;
                break;
            }
        }
        assertTrue("Catalogue should contain the item with ID 10000001", found);
    }

    @Test
    public void testListNotContainsItem() {
        String id = "UNKNOWN";
        List<Item> products = instance.listProducts();
        boolean found = false;
        for (Item item : products) {
            if (id.equals(item.getItemId())) {
                found = true;
                break;
            }
        }
        assertFalse("Catalogue does not contain the item with ID 10000001", found);
    }
}