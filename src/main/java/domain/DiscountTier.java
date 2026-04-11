package domain;

import java.math.BigDecimal;

public class DiscountTier {
    private int tierId;
    private int planId;
    private BigDecimal minAmount;
    private BigDecimal maxAmount; // null means no upper bound
    private BigDecimal discountRate;

    public DiscountTier() {
    }

    public DiscountTier(BigDecimal minAmount, BigDecimal maxAmount, BigDecimal discountRate) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.discountRate = discountRate;
    }

    public int getTierId() {
        return tierId;
    }

    public void setTierId(int tierId) {
        this.tierId = tierId;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }
}