package domain.report;

import java.math.BigDecimal;

/**
 * One line of the Stock‑Availability report.
 */
public class StockReportEntry {
    private final String itemId;
    private final String description;
    private final int quantityInStock;
    private final BigDecimal stockValue;   // quantity * package_cost
    private final BigDecimal vatAmount;    // 20% of stockValue

    public StockReportEntry(String itemId, String description,
                            int quantityInStock,
                            BigDecimal stockValue,
                            BigDecimal vatAmount) {
        this.itemId = itemId;
        this.description = description;
        this.quantityInStock = quantityInStock;
        this.stockValue = stockValue;
        this.vatAmount = vatAmount;
    }

    // -----------------------------------------------------------------
    // getters (no setters – immutable domain object)
    // -----------------------------------------------------------------
    public String getItemId()            { return itemId; }
    public String getDescription()       { return description; }
    public int    getQuantityInStock()   { return quantityInStock; }
    public BigDecimal getStockValue()    { return stockValue; }
    public BigDecimal getVatAmount()     { return vatAmount; }
}
