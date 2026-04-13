package service;

import dao.rpt.TurnoverReportDAO;
import dao.rpt.StockReportDAO;
import dao.rpt.DebtReportDAO;
import domain.report.TurnoverReportEntry;
import domain.report.StockReportEntry;
import domain.report.DebtReportEntry;

import java.time.LocalDate;
import java.util.List;

/**
 * Facade used by the UI. All heavy‑lifting stays in the DAO layer.
 */
public class ReportService {

    private final TurnoverReportDAO turnoverDao = new TurnoverReportDAO();
    private final StockReportDAO    stockDao    = new StockReportDAO();
    private final DebtReportDAO    debtDao    = new DebtReportDAO();

    // -----------------------------------------------------------------
    // Turnover
    // -----------------------------------------------------------------
    public TurnoverReportEntry generateTurnoverReport(LocalDate start, LocalDate end) {
        return turnoverDao.getTurnoverReport(start, end);
    }

    // -----------------------------------------------------------------
    // Stock Availability
    // -----------------------------------------------------------------
    public List<StockReportEntry> generateStockReport() {
        return stockDao.getStockReport();
    }

    // -----------------------------------------------------------------
    // Debt Aggregation
    // -----------------------------------------------------------------
    public DebtReportEntry generateDebtReport(LocalDate start, LocalDate end) {
        return debtDao.getDebtReport(start, end);
    }

    // -----------------------------------------------------------------
    // Optional pretty‑print helpers (used by the GUI)
    // -----------------------------------------------------------------
    public String formatTurnoverReport(TurnoverReportEntry e) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TURNOVER REPORT ===\n");
        sb.append("Period: ").append(e.getPeriodStart()).append(" – ").append(e.getPeriodEnd()).append("\n\n");
        sb.append(String.format("%-20s %10d%n", "Sales count:", e.getSalesCount()));
        sb.append(String.format("%-20s %10.2f%n", "Sales value:", e.getSalesValue()));
        sb.append(String.format("%-20s %10d%n", "Orders count:", e.getOrdersCount()));
        sb.append(String.format("%-20s %10.2f%n", "Orders value:", e.getOrdersValue()));
        return sb.toString();
    }

    public String formatStockReport(List<StockReportEntry> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== STOCK AVAILABILITY REPORT ===\n\n");
        sb.append(String.format("%-12s %-30s %10s %15s %15s%n",
                "Item ID", "Description", "Qty", "Value", "VAT"));
        sb.append("-".repeat(90)).append('\n');
        for (StockReportEntry e : list) {
            sb.append(String.format("%-12s %-30s %10d %15.2f %15.2f%n",
                    e.getItemId(),
                    e.getDescription(),
                    e.getQuantityInStock(),
                    e.getStockValue(),
                    e.getVatAmount()));
        }
        return sb.toString();
    }

    public String formatDebtReport(DebtReportEntry e) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEBT AGGREGATION REPORT ===\n");
        sb.append("Period: ").append(e.getPeriodStart()).append(" – ").append(e.getPeriodEnd()).append("\n\n");
        sb.append(String.format("%-30s %15.2f%n", "Beginning debt:", e.getBeginningDebt()));
        sb.append(String.format("%-30s %15.2f%n", "Payments received:", e.getPaymentsReceived()));
        sb.append(String.format("%-30s %15.2f%n", "New debt incurred:", e.getNewDebt()));
        sb.append(String.format("%-30s %15.2f%n", "Ending debt:", e.getEndingDebt()));
        return sb.toString();
    }
}
