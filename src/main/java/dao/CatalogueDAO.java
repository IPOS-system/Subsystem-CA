package dao;
import api_impl.DatabaseConnection;
import domain.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CatalogueDAO {

    public void updateCatalogue(List<Item> items) {
        String sql = """
                INSERT INTO CatalogueItems (
                    item_id,
                    description,
                    package_type,
                    unit,
                    units_in_pack,
                    package_cost,
                    availability
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    description = VALUES(description),
                    package_type = VALUES(package_type),
                    unit = VALUES(unit),
                    units_in_pack = VALUES(units_in_pack),
                    package_cost = VALUES(package_cost),
                    availability = VALUES(availability)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (Item item : items) {
                ps.setString(1, item.getItemId());
                ps.setString(2, item.getDescription());
                ps.setString(3, item.getPackageType());
                ps.setString(4, item.getUnit());
                ps.setInt(5, item.getUnitsInPack());
                ps.setBigDecimal(6, item.getPackageCost());
                ps.setInt(7, item.getQtyInStock()); //using qtyInStock as availability
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();

        String sql = """
                SELECT
                    item_id,
                    description,
                    package_type,
                    unit,
                    units_in_pack,
                    package_cost,
                    availability
                FROM CatalogueItems
                ORDER BY item_id
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

    public Item findById(String id) {
        String sql = """
                SELECT
                    item_id,
                    description,
                    package_type,
                    unit,
                    units_in_pack,
                    package_cost,
                    availability
                FROM CatalogueItems
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

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        return new Item(
                rs.getString("item_id"),
                rs.getString("description"),
                rs.getString("package_type"),
                rs.getString("unit"),
                rs.getInt("units_in_pack"),
                rs.getBigDecimal("package_cost"),
                rs.getInt("availability"),
                0,
                0
        );
    }
}