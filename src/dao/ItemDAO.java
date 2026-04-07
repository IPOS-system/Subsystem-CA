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
                    package_cost
                FROM Items
                ORDER BY description
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Item i = new Item();
                i.setItemId(rs.getString("item_id"));
                i.setDescription(rs.getString("description"));
                i.setPackageType(rs.getString("package_type"));
                i.setUnit(rs.getString("unit"));
                i.setUnitsInPack(rs.getInt("units_in_pack"));
                i.setPackageCost(rs.getBigDecimal("package_cost"));
                items.add(i);
            }

        } catch (SQLException e) {
            // print the stack‑trace.
            e.printStackTrace();
        }

        return items;
    }
}
