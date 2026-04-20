import org.junit.Test;
import service.AccountStatusService;
import service.DebtService;
import service.Result;
import service.TimeService;
import java.math.BigDecimal;
import static org.junit.Assert.*;

// Tests for checking payment validation in DebtService
// making sure invalid inputs are rejected before anything else
// no database needed here since it's just validation

public class DebtServiceTest {

    // basic setup just to create the service
    private final TimeService timeService = new TimeService();
    private final AccountStatusService accountStatusService = new AccountStatusService();
    private final DebtService debtService = new DebtService(timeService, accountStatusService);

    // null payment should fail
    @Test
    public void testNullPaymentAmountFails() {
        Result result = debtService.applyPayment("ACC-001", null);
        assertFalse("Null payment should fail", result.isSuccess());
    }

    // should also return some error message
    @Test
    public void testNullPaymentHasErrorMessage() {
        Result result = debtService.applyPayment("ACC-001", null);
        assertNotNull("Error message expected", result.getMessage());
        assertFalse("Message should not be empty", result.getMessage().isEmpty());
    }

    // zero payment doesn't make sense, should fail
    @Test
    public void testZeroPaymentFails() {
        Result result = debtService.applyPayment("ACC-001", BigDecimal.ZERO);
        assertFalse("Zero payment should fail", result.isSuccess());
    }

    // negative amount should fail
    @Test
    public void testNegativePaymentFails() {
        Result result = debtService.applyPayment("ACC-001", new BigDecimal("-50.00"));
        assertFalse("Negative payment should fail", result.isSuccess());
    }

    // even very small negative should fail
    @Test
    public void testTinyNegativePaymentFails() {
        Result result = debtService.applyPayment("ACC-001", new BigDecimal("-0.01"));
        assertFalse("Should reject -0.01", result.isSuccess());
    }

    // positive amount should pass validation (even if account doesn't exist)
    @Test
    public void testPositivePaymentPassesValidation() {
        Result result = debtService.applyPayment("ACC-DOESNOTEXIST", new BigDecimal("100.00"));

        // if it fails, it shouldn't be because of validation
        if (!result.isSuccess()) {
            assertNotEquals(
                    "Should not fail due to invalid amount",
                    "invalid payment amount",
                    result.getMessage()
            );
        }
    }
}