import domain.Item;
import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

// Tests Item class for constructor, getters, setters, and toString
public class ItemTest {

    // helper method to create a sample item for the tests
    private Item makeItem() {
        return new Item(
                "10000001",
                "Paracetamol",
                "Box",
                "Caps",
                20,
                new BigDecimal("0.10"),
                10345,
                300,
                10
        );
    }
    @Test
    public void testGetItemId() {
        Item item = makeItem();
        assertEquals("10000001", item.getItemId());
    }
    @Test
    public void testGetDescription() {
        Item item = makeItem();
        assertEquals("Paracetamol", item.getDescription());
    }
    @Test
    public void testGetPackageType() {
        Item item = makeItem();
        assertEquals("Box", item.getPackageType());
    }
    @Test
    public void testGetUnit() {
        Item item = makeItem();
        assertEquals("Caps", item.getUnit());
    }

    @Test
    public void testGetUnitsInPack() {
        Item item = makeItem();
        assertEquals(20, item.getUnitsInPack());
    }
    @Test
    public void testGetPackageCost() {
        Item item = makeItem();
        assertEquals(new BigDecimal("0.10"), item.getPackageCost());
    }
    @Test
    public void testGetQtyInStock() {
        Item item = makeItem();
        assertEquals(10345, item.getQtyInStock());
    }
    @Test
    public void testGetStockLimit() {
        Item item = makeItem();
        assertEquals(300, item.getStockLimit());
    }
    @Test
    public void testGetMarkup() {
        Item item = makeItem();
        assertEquals(10, item.getMarkup());
    }
    @Test
    public void testSetDescription() {      // checks that the description can be updated
        Item item = makeItem();
        item.setDescription("Paracetamol 500mg");
        assertEquals("Paracetamol 500mg", item.getDescription());
    }
    // checks that the package cost can be updated
    @Test
    public void testSetPackageCost() {
        Item item = makeItem();
        item.setPackageCost(new BigDecimal("0.15"));
        assertEquals(new BigDecimal("0.15"), item.getPackageCost());
    }
    @Test
    public void testToString() {    // Checking toString includes useful item details
        Item item = makeItem();
        String result = item.toString();
        assertTrue("toString should contain the description", result.contains("Paracetamol"));
        assertTrue("toString should contain the item ID", result.contains("10000001"));
    }
}