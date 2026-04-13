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

    //use when ipos sends us catalogue, and update catalogue in db
    public void updateCatalogue(){};

    public List<Item> findAll(){
        List<Item> items = new ArrayList<>();

        //select only the columns the UI needs.
        String sql = """
                SELECT
                    item_id,
                    description,
                    package_type,
                    unit,
                    units_in_pack,
                    package_cost
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
            // print the stack‑trace.
            e.printStackTrace();
        }

        return items;
    };

    public Item findById(String id){
        String sql = """
                SELECT
                    item_id,
                    description,
                    package_type,
                    unit,
                    units_in_pack,
                    package_cost
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
    };

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        return new Item(
                rs.getString("item_id"),
                rs.getString("description"),
                rs.getString("package_type"),
                rs.getString("unit"),
                rs.getInt("units_in_pack"),
                rs.getBigDecimal("package_cost"),
                0, //qtyInStock (not used)
                0, //stockLimit (not used)
                0  //markup (not used)
        );
    }
}
