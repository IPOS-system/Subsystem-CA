package domain.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregated debt figures for a given period.
 */
public class DebtReportEntry {
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal beginningDebt;      // debt owed before the period
    private final BigDecimal paymentsReceived;  // payments during the period
    private final BigDecimal newDebt;           // debt incurred during the period
    private final BigDecimal endingDebt;        // calculated = begin + new – payments

    public DebtReportEntry(LocalDate periodStart, LocalDate periodEnd,
                           BigDecimal beginningDebt,
                           BigDecimal paymentsReceived,
                           BigDecimal newDebt,
                           BigDecimal endingDebt) {
        this.periodStart      = periodStart;
        this.periodEnd        = periodEnd;
        this.beginningDebt    = beginningDebt;
        this.paymentsReceived = paymentsReceived;
        this.newDebt          = newDebt;
        this.endingDebt       = endingDebt;
    }

    public LocalDate getPeriodStart()      { return periodStart; }
    public LocalDate getPeriodEnd()        { return periodEnd; }
    public BigDecimal getBeginningDebt()   { return beginningDebt; }
    public BigDecimal getPaymentsReceived(){ return paymentsReceived; }
    public BigDecimal getNewDebt()         { return newDebt; }
    public BigDecimal getEndingDebt()      { return endingDebt; }
}
