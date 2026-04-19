import api.ISoldStockAPI;
import api_impl.ISoldStockAPIService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.After;
import org.junit.Test;
import service.Result;

import static org.junit.Assert.*;

// Subsystem tests for ISoldStockAPI provided interface
// This interface lets IPOS-PU and IPOS-SA check and deduct merchant stock
// Requires MySQL with the IPOS-CA schema loaded to run
public class ISoldStockAPITest {

    static ISoldStockAPIService implRef;
    static ISoldStockAPI instance;

    @BeforeClass
    public static void setUpClass() {
        implRef = new ISoldStockAPIService();
        instance = implRef; // testing through the interface
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // -- checkStock --

    @Test
    public void testCheckStockSufficientQuantity() {
        // 10000001 = Paracetamol, should have enough for 1
        Result result = instance.checkStock("10000001", 1);
        assertTrue("checkStock should succeed for available stock", result.isSuccess());
    }

    @Test
    public void testCheckStockInsufficientQuantity() {
        Result result = instance.checkStock("10000001", 999999);
        assertFalse("checkStock should fail when qty exceeds stock", result.isSuccess());
    }

    @Test
    public void testCheckStockInvalidItem() {
        Result result = instance.checkStock("INVALID_ID", 1);
        assertFalse("checkStock should fail for non-existent item", result.isSuccess());
    }

    // -- deductStock --

    @Test
    public void testDeductStockValidQuantity() {
        Result result = instance.deductStock("10000001", 1);
        assertTrue("deductStock should succeed for valid item and quantity", result.isSuccess());
    }

    @Test
    public void testDeductStockInvalidQuantity() {
        // deducting 0 should not be allowed
        Result result = instance.deductStock("10000001", 0);
        assertFalse("deductStock should fail for zero quantity", result.isSuccess());
    }

    @Test
    public void testDeductStockInvalidItem() {
        Result result = instance.deductStock("INVALID_ID", 1);
        assertFalse("deductStock should fail for non-existent item", result.isSuccess());
    }
}
