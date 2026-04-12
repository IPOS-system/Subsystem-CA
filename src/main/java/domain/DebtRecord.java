package domain;

import java.math.BigDecimal;

public class DebtRecord {
    private int debtId;
    private BigDecimal remaining;

    public DebtRecord(int debtId, BigDecimal remaining) {
        this.debtId = debtId;
        this.remaining = remaining;
    }

    public int getDebtId() { return debtId; }
    public BigDecimal getRemaining() { return remaining; }
}