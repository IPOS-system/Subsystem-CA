import domain.DiscountTier;
import org.junit.Test;
import service.DiscountPlanService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

// tests for DiscountPlanService
// checking that bad inputs are rejected
// no database needed here because this is just validation

public class DiscountPlanServiceTest {

    // one service object for all the tests
    private final DiscountPlanService service = new DiscountPlanService();


    // null rate should be rejected
    @Test
    public void testFixedPlanNullRateIsRejected() {
        try {
            service.saveFixedPlan("CUST001", null);
            fail("Should have thrown an error for null rate");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Wrong type of error: " + e.getClass().getSimpleName());
        }
    }

    // negative rate should also be rejected
    @Test
    public void testFixedPlanNegativeRateIsRejected() {
        try {
            service.saveFixedPlan("CUST001", new BigDecimal("-0.05"));
            fail("Should have thrown an error for negative rate");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Wrong type of error: " + e.getClass().getSimpleName());
        }
    }

    // zero should be allowed
    @Test
    public void testFixedPlanZeroRateIsAllowed() {
        try {
            service.saveFixedPlan("CUST001", BigDecimal.ZERO);
            // okay if it fails later because of database
        } catch (IllegalArgumentException e) {
            fail("Zero should be a valid rate but got rejected");
        } catch (Exception e) {
            // database not set up in tests
        }
    }

    // tiered plan tests

    // null list should be rejected
    @Test
    public void testTieredPlanNullListIsRejected() {
        try {
            service.saveTieredPlan("CUST001", null);
            fail("Should have thrown an error for null tiers");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("Wrong type of error: " + e.getClass().getSimpleName());
        }
    }

    // empty list should also be rejected
    @Test
    public void testTieredPlanEmptyListIsRejected() {
        try {
            service.saveTieredPlan("CUST001", new ArrayList<>());
            fail("Should have thrown an error for empty tiers list");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Wrong type of error: " + e.getClass().getSimpleName());
        }
    }

    // tier with null min should be rejected
    @Test
    public void testTieredPlanTierWithNullMinIsRejected() {
        List<DiscountTier> tiers = new ArrayList<>();
        // min is missing here
        tiers.add(new DiscountTier(null, new BigDecimal("1000"), new BigDecimal("0.05")));
        try {
            service.saveTieredPlan("CUST001", tiers);
            fail("Should have thrown an error for tier with no min amount");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Wrong type of error: " + e.getClass().getSimpleName());
        }
    }

    // tier with null rate should be rejected
    @Test
    public void testTieredPlanTierWithNullRateIsRejected() {
        List<DiscountTier> tiers = new ArrayList<>();
        // rate is missing here
        tiers.add(new DiscountTier(BigDecimal.ZERO, new BigDecimal("1000"), null));
        try {
            service.saveTieredPlan("CUST001", tiers);
            fail("Should have thrown an error for tier with no discount rate");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Wrong type of error: " + e.getClass().getSimpleName());
        }
    }

    // max less than min should be rejected
    @Test
    public void testTieredPlanTierMaxLessThanMinIsRejected() {
        List<DiscountTier> tiers = new ArrayList<>();
        // values are the wrong way round
        tiers.add(new DiscountTier(new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("0.05")));
        try {
            service.saveTieredPlan("CUST001", tiers);
            fail("Should have thrown an error when max is less than min");
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail("Wrong type of error: " + e.getClass().getSimpleName());
        }
    }

    // null max should be allowed
    @Test
    public void testTieredPlanNullMaxAmountIsAllowed() {
        List<DiscountTier> tiers = new ArrayList<>();
        // null max means no upper limit
        tiers.add(new DiscountTier(new BigDecimal("2000"), null, new BigDecimal("0.03")));
        try {
            service.saveTieredPlan("CUST001", tiers);
        } catch (IllegalArgumentException e) {
            fail("Null max (no upper limit) should be valid but got rejected");
        } catch (Exception e) {

        }
    }
}