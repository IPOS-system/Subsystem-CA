import domain.SaleItem;
import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

// Tests for the SaleItem class, checking values are stored correctly and total price is calculated properly

public class SaleItemTest {

    // helper method to create a sample item for reuse
    private SaleItem makeSaleItem() {
        return new SaleItem("10000001", "Paracetamol", 10, new BigDecimal("0.10"));
    }

    @Test
    public void testGetItemId() {
        SaleItem item = makeSaleItem();
        assertEquals("10000001", item.getItemId());
    }

    @Test
    public void testGetItemDescription() {
        SaleItem item = makeSaleItem();
        assertEquals("Paracetamol", item.getItemDescription());
    }

    @Test
    public void testGetQuantity() {
        SaleItem item = makeSaleItem();
        assertEquals(10, item.getQuantity());
    }

    @Test
    public void testGetUnitPrice() {
        SaleItem item = makeSaleItem();
        assertEquals(new BigDecimal("0.10"), item.getUnitPrice());
    }

    // check total price calculation (10 × 0.10 = 1.00)
    @Test
    public void testGetOrderItemPrice() {
        SaleItem item = makeSaleItem();
        assertEquals(new BigDecimal("1.00"), item.getOrderItemPrice());
    }

    @Test
    public void testGetOrderItemPriceLargerQuantity() {
        SaleItem item = new SaleItem("200-00005", "Rhynol", 5, new BigDecimal("2.50"));
        assertEquals(new BigDecimal("12.50"), item.getOrderItemPrice());
    }

    @Test
    public void testGetOrderItemPriceQuantityOfOne() {
        SaleItem item = new SaleItem("300-00001", "Ospen", 1, new BigDecimal("10.50"));
        assertEquals(new BigDecimal("10.50"), item.getOrderItemPrice());
    }

    @Test
    public void testUpdateQuantity() {
        SaleItem item = makeSaleItem();
        item.updateQuantity(20);
        assertEquals(20, item.getQuantity());
    }
    @Test
    public void testOrderItemPriceAfterQuantityUpdate() {       // check total updates after quantity change
        SaleItem item = makeSaleItem();
        item.updateQuantity(20);
        assertEquals(new BigDecimal("2.00"), item.getOrderItemPrice());
    }

    // just check setting sale id doesn't cause errors
    @Test
    public void testSetSaleId() {
        SaleItem item = makeSaleItem();
        item.setSaleId("SALE-001");
    }
}