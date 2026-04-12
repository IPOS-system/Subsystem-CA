package dao;

import domain.SaleItem;

import java.math.BigDecimal;
import java.sql.*;

public class SalesDAO {
    public int createSale(Connection con, String accountId, BigDecimal total, String paymentMethod, Integer debtId) {
        String sql = """
        INSERT INTO Sales (account_id, sale_date, total_amount, payment_method, debt_id)
        VALUES (?, CURRENT_DATE, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (accountId != null) {
                ps.setString(1, accountId);
            } else {
                ps.setNull(1, Types.VARCHAR);
            }

            ps.setBigDecimal(2, total);
            ps.setString(3, paymentMethod);

            if (debtId != null) {
                ps.setInt(4, debtId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void insertSaleItem(Connection con, int saleId, SaleItem item) {
        String sql = """
        INSERT INTO Sale_Items (sale_id, item_id, quantity)
        VALUES (?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            ps.setString(2, item.getItemId());
            ps.setInt(3, item.getQuantity());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
