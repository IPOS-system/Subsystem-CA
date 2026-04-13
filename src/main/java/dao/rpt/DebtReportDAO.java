package dao.rpt;

import api_impl.DatabaseConnection;
import domain.report.DebtReportEntry;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

/**
 * Calculates the three debt figures for a given period.
 */
public class DebtReportDAO {

    public DebtReportEntry getDebtReport(LocalDate start, LocalDate end) {
        // -----------------------------------------------------------------
        // 1) debt existing before the period (remaining_amount)
        // -----------------------------------------------------------------
        String sqlBegin = """
                SELECT COALESCE(SUM(remaining_amount),0) AS total
                FROM Monthly_Debts
                WHERE debt_month < ?
                """;

        // -----------------------------------------------------------------
        // 2) new debt incurred during the period
        // -----------------------------------------------------------------
        String sqlNew = """
                SELECT COALESCE(SUM(total_amount),0) AS total
                FROM Monthly_Debts
                WHERE debt_month BETWEEN ? AND ?
                """;

        // -----------------------------------------------------------------
        // 3) payments received during the period
        // -----------------------------------------------------------------
        String sqlPayments = """
                SELECT COALESCE(SUM(amount),0) AS total
                FROM Payments
                WHERE payment_date BETWEEN ? AND ?
                """;

        BigDecimal beginningDebt = BigDecimal.ZERO;
        BigDecimal newDebt       = BigDecimal.ZERO;
        BigDecimal payments     = BigDecimal.ZERO;

        Date sqlStart = Date.valueOf(start);
        Date sqlEnd   = Date.valueOf(end);

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement psBegin    = con.prepareStatement(sqlBegin);
             PreparedStatement psNew      = con.prepareStatement(sqlNew);
             PreparedStatement psPayments = con.prepareStatement(sqlPayments)) {

            // ---- beginning debt -------------------------------------------------
            psBegin.setDate(1, sqlStart);
            try (ResultSet rs = psBegin.executeQuery()) {
                if (rs.next()) {
                    beginningDebt = rs.getBigDecimal("total");
                }
            }

            // ---- new debt -------------------------------------------------------
            psNew.setDate(1, sqlStart);
            psNew.setDate(2, sqlEnd);
            try (ResultSet rs = psNew.executeQuery()) {
                if (rs.next()) {
                    newDebt = rs.getBigDecimal("total");
                }
            }

            // ---- payments -------------------------------------------------------
            psPayments.setDate(1, sqlStart);
            psPayments.setDate(2, sqlEnd);
            try (ResultSet rs = psPayments.executeQuery()) {
                if (rs.next()) {
                    payments = rs.getBigDecimal("total");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ending debt = beginning + new – payments
        BigDecimal endingDebt = beginningDebt.add(newDebt).subtract(payments);

        return new DebtReportEntry(start, end,
                beginningDebt, payments, newDebt, endingDebt);
    }
}
