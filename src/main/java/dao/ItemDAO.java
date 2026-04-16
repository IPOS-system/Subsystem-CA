package dao;

import api_impl.DatabaseConnection;
import domain.Item;
import service.Result;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Reads catalogue
 * More methods later:
 * (e.g. findById, updateStock, etc.).
 */
public class ItemDAO {

    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();

        //select only the columns the UI needs.
        String sql = """
                SELECT
                    item_id,
                    description,
                    package_type,
                    unit,
                    units_in_pack,
                    package_cost,
                    quantity_in_stock,
                    stock_limit,
                    markup
                    
                    
                FROM Items
                ORDER BY item_id
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(mapRowToItem(rs));
            }

        } catch (SQLException e) {
            // print the stack‑trace.
            e.printStackTrace();
        }

        return items;
    }



    public Item findById(String id) {
        String sql = """
                SELECT
                    item_id,
                    description,
                    package_type,
                    unit,
                    units_in_pack,
                    package_cost,
                    quantity_in_stock,
                    stock_limit,
                    markup
                FROM Items
                WHERE item_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addItemToStock(Item i){
        String sql = """
        INSERT INTO Items (
            item_id, description, package_type, unit,
            units_in_pack, package_cost,
            quantity_in_stock, stock_limit, markup
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, i.getItemId());
            ps.setString(2, i.getDescription());
            ps.setString(3, i.getPackageType());
            ps.setString(4, i.getUnit());
            ps.setInt(5, i.getUnitsInPack());
            ps.setBigDecimal(6, i.getPackageCost());
            ps.setInt(7, i.getQtyInStock());
            ps.setInt(8, i.getStockLimit());
            ps.setBigDecimal(9, BigDecimal.valueOf(i.getMarkup()));

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean modifyQtyInStock(Connection con, String itemId, int newQty) {
        String sql = "UPDATE Items SET quantity_in_stock = ? WHERE item_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, newQty);
            ps.setString(2, itemId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean modifyQtyInStock(String itemId, int newQty) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return modifyQtyInStock(con, itemId, newQty);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean  removeItemFromStock(String itemId) {
        String sql = "DELETE FROM Items WHERE item_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, itemId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    private Item mapRowToItem(ResultSet rs) throws SQLException {
        return new Item(
                rs.getString("item_id"),
                rs.getString("description"),
                rs.getString("package_type"),
                rs.getString("unit"),
                rs.getInt("units_in_pack"),
                rs.getBigDecimal("package_cost"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("stock_limit"),
                rs.getInt("markup")
        );
    }

    public Result reduceStock(Connection con, String itemId, int quantity) {
        String sql = """
        UPDATE Items
        SET quantity_in_stock = quantity_in_stock - ?
        WHERE item_id = ?
        AND quantity_in_stock >= ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setString(2, itemId);
            ps.setInt(3, quantity);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated == 0) {
                return Result.fail("not enough stock for item " + itemId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        }

        return Result.success("stock reduced");
    }

    public Result reduceStock(String itemId, int quantity) {
        try (Connection con = DatabaseConnection.getConnection()) {
            return reduceStock(con, itemId, quantity);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.fail("database error");
        }
    }

    public List<Item> findLowStockItems() {
        List<Item> items = new ArrayList<>();

        String sql = """
            SELECT
                item_id,
                description,
                package_type,
                unit,
                units_in_pack,
                package_cost,
                quantity_in_stock,
                stock_limit,
                markup
            FROM Items
            WHERE quantity_in_stock < stock_limit
            ORDER BY quantity_in_stock ASC
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(mapRowToItem(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }


}
