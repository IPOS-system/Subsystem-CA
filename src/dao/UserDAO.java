package dao;

import domain.User;
import api_impl.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    //used by login
    public User findByUsername(String username) {
        String sql = "SELECT * FROM LocalUser WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }
            return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
            );

        } catch (SQLException e) {
            System.out.println("//error finding user by username: " + e.getMessage());
            return null;
        }
    }
}