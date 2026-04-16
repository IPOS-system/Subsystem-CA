package dao;


import api_impl.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineOrderDAO {

    public boolean saveOnlineOrder(String orderId, String status, String deliveryAddress,
                                   List<Map<String, Object>> items) {
        String orderSql = """
                INSERT INTO OnlineOrders (order_id, status, delivery_address)
                VALUES (?, ?, ?)
                """;

        String itemSql = """
                INSERT INTO OnlineOrderItems (order_id, product_id, quantity)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement orderPs = con.prepareStatement(orderSql);
                 PreparedStatement itemPs = con.prepareStatement(itemSql)) {

                orderPs.setString(1, orderId);
                orderPs.setString(2, status);
                orderPs.setString(3, deliveryAddress);
                orderPs.executeUpdate();

                for (Map<String, Object> item : items) {
                    itemPs.setString(1, orderId);
                    itemPs.setString(2, item.get("productId").toString().trim());
                    itemPs.setInt(3, ((Number) item.get("quantity")).intValue());
                    itemPs.addBatch();
                }

                itemPs.executeBatch();
                con.commit();
                return true;

            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> findAllOrders() {
        List<Map<String, Object>> orders = new ArrayList<>();

        String sql = """
            SELECT order_id, status, delivery_address, received_at
            FROM OnlineOrders
            ORDER BY received_at DESC
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("orderId", rs.getString("order_id"));
                row.put("status", rs.getString("status"));
                row.put("deliveryAddress", rs.getString("delivery_address"));
                row.put("receivedAt", rs.getTimestamp("received_at"));
                orders.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Map<String, Object>> findItemsByOrderId(String orderId) {
        List<Map<String, Object>> items = new ArrayList<>();

        String sql = """
            SELECT product_id, quantity
            FROM OnlineOrderItems
            WHERE order_id = ?
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("productId", rs.getString("product_id"));
                    row.put("quantity", rs.getInt("quantity"));
                    items.add(row);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }
}