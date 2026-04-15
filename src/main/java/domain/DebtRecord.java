package domain;

import java.math.BigDecimal;
import java.sql.Date;

public class DebtRecord {
    private int debtId;
    private BigDecimal remaining;
    private String firstReminder;
    private Date currentMonth;

    public String getSecondReminder() {
        return secondReminder;
    }

    private String secondReminder;


    public String getFirstReminder() {
        return firstReminder;
    }

    public Date getCurrentMonth() {
        return currentMonth;
    }

    public DebtRecord(int debtId, BigDecimal remaining, String firstReminder, String secondReminder, Date currentMonth) {
        this.debtId = debtId;
        this.remaining = remaining;
        this.firstReminder =  firstReminder;
        this.secondReminder = secondReminder;
        this.currentMonth = currentMonth;
    }

    public int getDebtId() { return debtId; }
    public BigDecimal getRemaining() { return remaining; }
}