
package dao;

import api_impl.DatabaseConnection;
import domain.CustomerAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerAccountDAO {

    public boolean createCustomerAccount(CustomerAccount c) {
        String sql = """
                INSERT INTO Customers (
                    account_id,
                    account_holder_name,
                    contact_name,
                    address,
                    phone,
                    credit_limit,
                    account_status
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getAccountId());
            ps.setString(2, c.getAccountHolderName());
            ps.setString(3, c.getContactName());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getPhone());
            ps.setBigDecimal(6, c.getCreditLimit());
            ps.setString(7, c.getAccountStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //this only changes their status to closed
    //since when you delete you cant delete them
    //or all their sales and debts will become anonymous.
    public boolean deleteCustomerAccount(String accountId) {
        String sql = "UPDATE Customers SET account_status = ? WHERE account_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "closed");
            ps.setString(2, accountId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CustomerAccount> getAllCustomerAccounts() {
        List<CustomerAccount> list = new ArrayList<>();

        String sql = """
                SELECT
                    c.account_id,
                    c.account_holder_name,
                    c.contact_name,
                    c.address,
                    c.phone,
                    c.credit_limit,
                    dp.plan_id,
                    dp.plan_type,
                    c.account_status
                FROM Customers c
                LEFT JOIN Discount_Plans dp
                    ON dp.account_id = c.account_id
                WHERE c.account_status = ? OR c.account_status = ? OR account_status = ?
                ORDER BY c.account_holder_name
                """;

        try (Connection con = DatabaseConnection.getConnection();

             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "active");
            ps.setString(2, "suspended");
            ps.setString(3, "in default");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CustomerAccount c = new CustomerAccount();

                c.setAccountId(rs.getString("account_id"));
                c.setAccountHolderName(rs.getString("account_holder_name"));
                c.setContactName(rs.getString("contact_name"));
                c.setAddress(rs.getString("address"));
                c.setPhone(rs.getString("phone"));
                c.setCreditLimit(rs.getBigDecimal("credit_limit"));
                c.setAgreedDiscountId((Integer) rs.getObject("plan_id"));
                c.setDiscountPlanType(rs.getString("plan_type"));
                c.setAccountStatus(rs.getString("account_status"));

                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public CustomerAccount findById(String accountId) {
        String sql = """
                SELECT
                    c.account_id,
                    c.account_holder_name,
                    c.contact_name,
                    c.address,
                    c.phone,
                    c.credit_limit,
                    dp.plan_id,
                    dp.plan_type,
                    c.account_status
                FROM Customers c
                LEFT JOIN Discount_Plans dp
                    ON dp.account_id = c.account_id
                WHERE c.account_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CustomerAccount c = new CustomerAccount();

                    c.setAccountId(rs.getString("account_id"));
                    c.setAccountHolderName(rs.getString("account_holder_name"));
                    c.setContactName(rs.getString("contact_name"));
                    c.setAddress(rs.getString("address"));
                    c.setPhone(rs.getString("phone"));
                    c.setCreditLimit(rs.getBigDecimal("credit_limit"));
                    c.setAgreedDiscountId((Integer) rs.getObject("plan_id"));
                    c.setDiscountPlanType(rs.getString("plan_type"));
                    c.setAccountStatus(rs.getString("account_status"));

                    return c;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateCustomerAccount(CustomerAccount c) {
        String sql = """
                UPDATE Customers
                SET
                    account_holder_name = ?,
                    contact_name = ?,
                    address = ?,
                    phone = ?,
                    credit_limit = ?,
                    account_status = ?
                WHERE account_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getAccountHolderName());
            ps.setString(2, c.getContactName());
            ps.setString(3, c.getAddress());
            ps.setString(4, c.getPhone());
            ps.setBigDecimal(5, c.getCreditLimit());
            ps.setString(6, c.getAccountStatus());
            ps.setString(7, c.getAccountId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAccountsInDefault() {
        String sql = """
        UPDATE Customers c
        SET c.account_status = 'in default'
        WHERE c.account_status <> 'closed'
          AND EXISTS (
              SELECT 1
              FROM Monthly_Debts md
              WHERE md.account_id = c.account_id
                AND md.remaining_amount > 0
                AND md.status_2ndReminder = 'due'
          )
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            return ps.executeUpdate() >= 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAccountsSuspended() {
        String sql = """
        UPDATE Customers c
        SET c.account_status = 'suspended'
        WHERE c.account_status NOT IN ('closed', 'in default')
          AND EXISTS (
              SELECT 1
              FROM Monthly_Debts md
              WHERE md.account_id = c.account_id
                AND md.remaining_amount > 0
                AND md.status_1stReminder = 'due'
          )
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            return ps.executeUpdate() >= 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAccountsActiveIfClear() {
        String sql = """
    UPDATE Customers c
    SET c.account_status = 'active'
    WHERE c.account_status IN ('suspended', 'in default')
      AND NOT EXISTS (
          SELECT 1
          FROM Monthly_Debts md
          WHERE md.account_id = c.account_id
            AND md.remaining_amount > 0
            AND (md.status_1stReminder = 'due' OR md.status_2ndReminder = 'due')
      )
    """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            return ps.executeUpdate() >= 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}