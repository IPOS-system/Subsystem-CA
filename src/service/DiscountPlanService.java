package service;

import dao.DiscountPlanDAO;
import domain.DiscountPlan;
import domain.DiscountTier;

import java.math.BigDecimal;
import java.util.List;

public class DiscountPlanService {
    private final DiscountPlanDAO discountPlanDAO;

    public DiscountPlanService() {
        this.discountPlanDAO = new DiscountPlanDAO();
    }

    public DiscountPlan getCustomerDiscountPlan(String customerId) throws Exception {
        return discountPlanDAO.getCustomerDiscountPlan(customerId);
    }

    public void clearCustomerDiscountPlan(String customerId) throws Exception {
        discountPlanDAO.clearCustomerDiscountPlan(customerId);
    }

    public void saveFixedPlan(String customerId, BigDecimal rate) throws Exception {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Fixed rate must be 0 or more.");
        }

        discountPlanDAO.saveFixedPlanForCustomer(
                customerId,
                "Fixed plan for " + customerId,
                rate
        );
    }

    public void saveTieredPlan(String customerId, List<DiscountTier> tiers) throws Exception {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("At least one tier is required.");
        }

        for (DiscountTier tier : tiers) {
            if (tier.getMinAmount() == null || tier.getDiscountRate() == null) {
                throw new IllegalArgumentException("Each tier needs min amount and discount rate.");
            }
            if (tier.getMaxAmount() != null &&
                    tier.getMaxAmount().compareTo(tier.getMinAmount()) < 0) {
                throw new IllegalArgumentException("Tier max cannot be less than min.");
            }
        }

        discountPlanDAO.saveTieredPlanForCustomer(
                customerId,
                "Tiered plan for " + customerId,
                tiers
        );
    }
}