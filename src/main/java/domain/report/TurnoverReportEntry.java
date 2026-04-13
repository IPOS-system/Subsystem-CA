package domain.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO used by the Turnover report.
 *
 * It contains the aggregated figures for a date range:
 *   – number of sales and total sales value,
 *   – number of orders and total order value.
 *
 * The class is immutable – all fields are final and only getters are provided.
 */
public class TurnoverReportEntry {

    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final int       salesCount;
    private final BigDecimal salesValue;
    private final int       ordersCount;
    private final BigDecimal ordersValue;

    public TurnoverReportEntry(LocalDate periodStart, LocalDate periodEnd,
                               int salesCount, BigDecimal salesValue,
                               int ordersCount, BigDecimal ordersValue) {
        this.periodStart = periodStart;
        this.periodEnd   = periodEnd;
        this.salesCount  = salesCount;
        this.salesValue  = salesValue;
        this.ordersCount = ordersCount;
        this.ordersValue = ordersValue;
    }

    // -----------------------------------------------------------------
    // Getters – the service layer uses them to format the report.
    // -----------------------------------------------------------------
    public LocalDate getPeriodStart()   { return periodStart; }
    public LocalDate getPeriodEnd()     { return periodEnd;   }
    public int       getSalesCount()    { return salesCount;  }
    public BigDecimal getSalesValue()   { return salesValue;  }
    public int       getOrdersCount()   { return ordersCount; }
    public BigDecimal getOrdersValue()  { return ordersValue; }
}
