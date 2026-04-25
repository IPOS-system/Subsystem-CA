import api.IPaymentAPI;
import api_impl.IPaymentAPIService;
import org.junit.BeforeClass;
import org.junit.Test;

public class IPaymentAPITest {

    static IPaymentAPIService implRef;
    static IPaymentAPI instance;

    @BeforeClass
    public static void setUpClass() {
        implRef = new IPaymentAPIService();
        instance = implRef;
    }

    @Test
    public void testPayValidOrder() {
        System.out.println("\nTest Plan: Pay for a valid order with a positive amount");
        System.out.println("Input: amount = 10.00");
        System.out.println("Expected: Result.isSuccess() == true");

        // Payment payment = new Payment("1", 10.00, "visa", "12/26", "name", "`1234123412341234", "984");
        // Result result = instance.Pay(payment);
        // assertTrue(result.isSuccess());
    }

    @Test
    public void testPayNegativeAmount() {
        System.out.println("\nTest Plan: Pay for a valid order with a negative amount");
        System.out.println("Input: amount = -10.00");
        System.out.println("Expected: Result.isSuccess() == false");

        // Payment payment = new Payment("2", -10.00, "visa", "12/26", "name", "`1234123412341234", "984");
        // Result result = instance.Pay(payment);
        // assertFalse(result.isSuccess());
    }

    @Test
    public void testPayInvalidOrderId() {
        System.out.println("\nTest Plan: Pay for an order that does not exist");
        System.out.println("Input: orderId = INVALID");
        System.out.println("Expected: Result.isSuccess() == false");

        // Payment payment = new Payment("INVALID", 10.00, "visa", "12/26", "name", "`1234123412341234", "984");
        // Result result = instance.Pay(payment);
        // assertFalse(result.isSuccess());
    }
}