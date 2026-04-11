package domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DiscountPlan {
    public static final String TYPE_FIXED = "fixed";
    public static final String TYPE_TIERED = "tiered";

    private int planId;
    private String planName;
    private String planType;
    private BigDecimal fixedRate;
    private List<DiscountTier> tiers = new ArrayList<>();

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public BigDecimal getFixedRate() {
        return fixedRate;
    }

    public void setFixedRate(BigDecimal fixedRate) {
        this.fixedRate = fixedRate;
    }

    public List<DiscountTier> getTiers() {
        return tiers;
    }

    public void setTiers(List<DiscountTier> tiers) {
        this.tiers = tiers;
    }
}