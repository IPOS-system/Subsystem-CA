import org.junit.Test;
import service.AccountStatusService;
import service.DebtService;
import service.Result;
import service.TimeService;
import java.math.BigDecimal;
import static org.junit.Assert.*;

// Tests for payment validation in DebtService

public class DebtServiceTest {

    // setting up the service needed for the tests
    private final TimeService timeService = new TimeService();
    private final AccountStatusService accountStatusService = new AccountStatusService();
    private final DebtService debtService = new DebtService(timeService, accountStatusService);

    @Test
    public void testNullPaymentAmountFails() {
        Result result = debtService.applyPayment("ACC-001", null);
        assertFalse("Null payment should fail", result.isSuccess());
    }

    @Test
    public void testNullPaymentHasErrorMessage() {
        Result result = debtService.applyPayment("ACC-001", null);
        assertNotNull("There should be an error message", result.getMessage());
        assertFalse("Error message should not be blank", result.getMessage().isEmpty());
    }

    @Test
    public void testZeroPaymentFails() {   // £0 payment doesn't make sense, so it should fail
        Result result = debtService.applyPayment("ACC-001", BigDecimal.ZERO);
        assertFalse("Zero payment should fail", result.isSuccess());
    }

    @Test
    public void testNegativePaymentFails() {
        Result result = debtService.applyPayment("ACC-001", new BigDecimal("-50.00"));
        assertFalse("Negative payment should fail", result.isSuccess());
    }

    @Test
    public void testTinyNegativePaymentFails() {
        Result result = debtService.applyPayment("ACC-001", new BigDecimal("-0.01"));
        assertFalse("Even -0.01 should be rejected", result.isSuccess());
    }

    // valid amount should pass input checks (even if account doesn't exist)
    @Test
    public void testPositivePaymentPassesValidation() {
        Result result = debtService.applyPayment("ACC-DOESNOTEXIST", new BigDecimal("100.00"));
        
        // if it fails, it's probably because the account doesn't exist
        if (!result.isSuccess()) {
            assertNotEquals(
                    "Should not fail with input validation message for a valid amount",
                    "invalid payment amount",
                    result.getMessage()
            );
        }
    }
}