package dao.rpt;

import api_impl.DatabaseConnection;
import domain.report.TurnoverReportEntry;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

/**
 * Queries the DB for sales‑ and order‑turnover data.
 */
public class TurnoverReportDAO {

    public TurnoverReportEntry getTurnoverReport(LocalDate start, LocalDate end) {
        String salesSql = """
                SELECT COALESCE(COUNT(*),0) AS cnt,
                       COALESCE(SUM(total_amount),0) AS total
                FROM Sales
                WHERE sale_date BETWEEN ? AND ?
                """;

        String ordersSql = """
                SELECT COALESCE(COUNT(DISTINCT o.order_id),0) AS cnt,
                       COALESCE(SUM(oi.quantity * i.package_cost),0) AS total
                FROM Orders o
                JOIN OrderItems oi ON o.order_id = oi.order_id
                JOIN Items i       ON oi.item_id = i.item_id
                WHERE o.order_date BETWEEN ? AND ?
                """;

        int salesCount = 0;
        BigDecimal salesValue = BigDecimal.ZERO;
        int ordersCount = 0;
        BigDecimal ordersValue = BigDecimal.ZERO;

        Date sqlStart = Date.valueOf(start);
        Date sqlEnd   = Date.valueOf(end);

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement psSales  = con.prepareStatement(salesSql);
             PreparedStatement psOrders = con.prepareStatement(ordersSql)) {

            // -------- sales ----------
            psSales.setDate(1, sqlStart);
            psSales.setDate(2, sqlEnd);
            try (ResultSet rs = psSales.executeQuery()) {
                if (rs.next()) {
                    salesCount = rs.getInt("cnt");
                    salesValue = rs.getBigDecimal("total");
                }
            }

            // -------- orders ----------
            psOrders.setDate(1, sqlStart);
            psOrders.setDate(2, sqlEnd);
            try (ResultSet rs = psOrders.executeQuery()) {
                if (rs.next()) {
                    ordersCount = rs.getInt("cnt");
                    ordersValue = rs.getBigDecimal("total");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();               // prototype – log to console
        }

        return new TurnoverReportEntry(start, end,
                salesCount, salesValue,
                ordersCount, ordersValue);
    }
}
