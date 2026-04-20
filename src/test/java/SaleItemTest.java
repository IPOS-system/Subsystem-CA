import domain.SaleItem;
import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

// This file tests the SaleItem class.
// A SaleItem represents one item in a sale - for example
// "10 packs of Paracetamol at £0.10 each"
// We test that it stores values correctly and calculates the total price right.

public class SaleItemTest {

    // a helper method to make a basic SaleItem we can reuse in tests
    private SaleItem makeSaleItem() {
        return new SaleItem("10000001", "Paracetamol", 10, new BigDecimal("0.10"));
    }

    // Test 1: the item ID we pass in should come back out the same
    @Test
    public void testGetItemId() {
        SaleItem item = makeSaleItem();
        assertEquals("10000001", item.getItemId());
    }

    // Test 2: the description we pass in should come back out the same
    @Test
    public void testGetItemDescription() {
        SaleItem item = makeSaleItem();
        assertEquals("Paracetamol", item.getItemDescription());
    }

    // Test 3: the quantity we pass in should come back out the same
    @Test
    public void testGetQuantity() {
        SaleItem item = makeSaleItem();
        assertEquals(10, item.getQuantity());
    }

    // Test 4: the unit price we pass in should come back out the same
    @Test
    public void testGetUnitPrice() {
        SaleItem item = makeSaleItem();
        assertEquals(new BigDecimal("0.10"), item.getUnitPrice());
    }

    // Test 5: total price = unit price x quantity
    // so 10 packs at £0.10 each should = £1.00
    @Test
    public void testGetOrderItemPrice() {
        SaleItem item = makeSaleItem();
        assertEquals(new BigDecimal("1.00"), item.getOrderItemPrice());
    }

    // Test 6: check total price with a bigger quantity
    // 5 packs at £2.50 each should = £12.50
    @Test
    public void testGetOrderItemPriceLargerQuantity() {
        SaleItem item = new SaleItem("200-00005", "Rhynol", 5, new BigDecimal("2.50"));
        assertEquals(new BigDecimal("12.50"), item.getOrderItemPrice());
    }

    // Test 7: check total price with quantity of 1
    // 1 pack at £10.50 should = £10.50
    @Test
    public void testGetOrderItemPriceQuantityOfOne() {
        SaleItem item = new SaleItem("300-00001", "Ospen", 1, new BigDecimal("10.50"));
        assertEquals(new BigDecimal("10.50"), item.getOrderItemPrice());
    }

    // Test 8: we should be able to update the quantity after creating the item
    @Test
    public void testUpdateQuantity() {
        SaleItem item = makeSaleItem();
        item.updateQuantity(20);
        assertEquals(20, item.getQuantity());
    }

    // Test 9: after updating quantity, the total price should also update
    // was 10 x £0.10 = £1.00, now should be 20 x £0.10 = £2.00
    @Test
    public void testOrderItemPriceAfterQuantityUpdate() {
        SaleItem item = makeSaleItem();
        item.updateQuantity(20);
        assertEquals(new BigDecimal("2.00"), item.getOrderItemPrice());
    }

    // Test 10: we should be able to set a sale ID on the item
    @Test
    public void testSetSaleId() {
        SaleItem item = makeSaleItem();
        item.setSaleId("SALE-001");
        // no getter for saleId but setting it should not crash
    }
}
