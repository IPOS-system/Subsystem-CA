package dao;

import domain.SaleItem;

import java.math.BigDecimal;
import java.sql.*;

public class SalesDAO {
    public int createSale(Connection con, String accountId, BigDecimal total, String paymentMethod, String cardNumber, String expiry, Integer debtId, Date currentDate) {
        String sql = """
        INSERT INTO Sales (account_id, sale_date, total_amount, payment_method, debt_id,  card_first_digits, card_last_digits, expiry_mm_yy)
        VALUES (?, ?, ?, ?, ?, ?,?,?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (accountId != null) {
                ps.setString(1, accountId);
            } else {
                ps.setNull(1, Types.VARCHAR);
            }

            ps.setDate(2, currentDate);
            ps.setBigDecimal(3, total);


            ps.setString(4, paymentMethod);

            if (debtId != null) {
                ps.setInt(5, debtId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }


            String first4 = null;
            String last4 = null;
            String cardexp = null;

            if (cardNumber != null && cardNumber.length() == 16) {
                first4 = cardNumber.substring(0, 4);
                last4 = cardNumber.substring(12, 16);
                cardexp = expiry;
            }

            if (first4 != null) {
                ps.setString(6, first4);
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            if (last4 != null) {
                ps.setString(7, last4);
            } else {
                ps.setNull(7, Types.VARCHAR);
            }

            if (expiry != null) ps.setString(8, cardexp); else ps.setNull(8, Types.VARCHAR);








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
