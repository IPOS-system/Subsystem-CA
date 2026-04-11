package dao;

import api_impl.DatabaseConnection;
import domain.Item;

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

        // select only the columns the UI needs.
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

    public boolean modifyQtyInStock(String itemId, int newQty) {
        String sql = "UPDATE Items SET quantity_in_stock = ? WHERE item_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, newQty);
            ps.setString(2, itemId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
}
