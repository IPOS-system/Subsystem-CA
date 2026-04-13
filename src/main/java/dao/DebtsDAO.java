package dao;

import api_impl.DatabaseConnection;
import domain.DebtRecord;
import service.Result;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DebtsDAO {

    public List<DebtRecord> getDebts(String accountId) {
        List<DebtRecord> debts = new ArrayList<>();

        String sql = """
            SELECT debt_id, remaining_amount
            FROM Monthly_Debts
            WHERE account_id = ?
            AND remaining_amount > 0
            ORDER BY debt_month ASC
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    debts.add(new DebtRecord(
                            rs.getInt("debt_id"),
                            rs.getBigDecimal("remaining_amount")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return debts;
    }

    public Result updateRemaining(int debtId, BigDecimal newAmount) {
        String sql = """
            UPDATE Monthly_Debts
            SET remaining_amount = ?
            WHERE debt_id = ?
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, newAmount);
            ps.setInt(2, debtId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return  Result.fail(e.getMessage());
        }
        return Result.success("OK");
    }

    public DebtRecord getCurrentMonthDebt(Connection con, String accountId, Date month) {
        String sql = """
        SELECT debt_id, remaining_amount
        FROM Monthly_Debts
        WHERE account_id = ?
        AND debt_month = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ps.setDate(2, month);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DebtRecord(
                            rs.getInt("debt_id"),
                            rs.getBigDecimal("remaining_amount")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int createDebt(Connection con, String accountId, Date month, Date dueDate, BigDecimal amount) {
        String sql = """
        INSERT INTO Monthly_Debts (account_id, debt_month, due_date, total_amount, remaining_amount)
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, accountId);
            ps.setDate(2, month);
            ps.setDate(3, dueDate);
            ps.setBigDecimal(4, amount);
            ps.setBigDecimal(5, amount);

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

    //used to share con when making transactions.
    public void addToDebt(Connection con, int debtId, BigDecimal amount) {
        String sql = """
        UPDATE Monthly_Debts
        SET total_amount = total_amount + ?,
            remaining_amount = remaining_amount + ?
        WHERE debt_id = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, amount);
            ps.setBigDecimal(2, amount);
            ps.setInt(3, debtId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addToDebt(int debtId, BigDecimal amount) {
        try (Connection con = DatabaseConnection.getConnection()) {
            addToDebt(con, debtId, amount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}