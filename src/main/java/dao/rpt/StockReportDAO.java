package dao.rpt;

import api_impl.DatabaseConnection;
import domain.report.StockReportEntry;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads every item from the stock table and calculates its value and VAT.
 */
public class StockReportDAO {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.20"); // 20%

    public List<StockReportEntry> getStockReport() {
        String sql = """
                SELECT item_id, description, quantity_in_stock, package_cost
                FROM Items
                ORDER BY item_id
                """;

        List<StockReportEntry> list = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String itemId   = rs.getString("item_id");
                String desc     = rs.getString("description");
                int    qty      = rs.getInt("quantity_in_stock");
                BigDecimal price = rs.getBigDecimal("package_cost");

                BigDecimal stockValue = price.multiply(BigDecimal.valueOf(qty));
                BigDecimal vatAmount = stockValue.multiply(VAT_RATE);

                list.add(new StockReportEntry(itemId, desc, qty, stockValue, vatAmount));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
