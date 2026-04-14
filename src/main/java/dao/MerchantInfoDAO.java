//package dao;
//
//import api_impl.DatabaseConnection;
//import domain.MerchantTemplate;
//
//import java.sql.*;
//
//
//public class MerchantInfoDAO {
//
//    private static final int SINGLETON_ID = 1;
//
//    /** Load the singleton record – if none exists a blank object is returned. */
//    public MerchantTemplate getInfo() {
//        String sql = "SELECT pharmacy_name, address, email, logo_path FROM MerchantInfo WHERE id = ?";
//        try (Connection con = DatabaseConnection.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, SINGLETON_ID);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    MerchantTemplate mi = new MerchantTemplate();
//                    mi.setPharmacyName(rs.getString("pharmacy_name"));
//                    mi.setAddress(rs.getString("address"));
//                    mi.setEmail(rs.getString("email"));
//                    mi.setLogoPath(rs.getString("logo_path"));
//                    return mi;
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        // No row – return the empty placeholder object
//        return new MerchantTemplate();
//    }
//
//    /** Insert or update the singleton row. */
//    public boolean save(MerchantTemplate info) {
//        // Try an UPDATE first; if nothing changed we fallback to INSERT.
//        String update = "UPDATE MerchantInfo SET pharmacy_name = ?, address = ?, email = ?, logo_path = ? WHERE id = ?";
//        try (Connection con = DatabaseConnection.getConnection();
//             PreparedStatement ps = con.prepareStatement(update)) {
//
//            ps.setString(1, info.getPharmacyName());
//            ps.setString(2, info.getAddress());
//            ps.setString(3, info.getEmail());
//            ps.setString(4, info.getLogoPath());
//            ps.setInt(5, SINGLETON_ID);
//            int rows = ps.executeUpdate();
//            if (rows > 0) return true;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        // If UPDATE failed (row does not exist) we INSERT.
//        String insert = "INSERT INTO MerchantInfo (id, pharmacy_name, address, email, logo_path) VALUES (?,?,?,?,?)";
//        try (Connection con = DatabaseConnection.getConnection();
//             PreparedStatement ps = con.prepareStatement(insert)) {
//
//            ps.setInt(1, SINGLETON_ID);
//            ps.setString(2, info.getPharmacyName());
//            ps.setString(3, info.getAddress());
//            ps.setString(4, info.getEmail());
//            ps.setString(5, info.getLogoPath());
//            return ps.executeUpdate() > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}
