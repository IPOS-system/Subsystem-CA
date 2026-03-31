package dao;

import domain.User;
import api_impl.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//TODO add things like getallusers, createuser, updaterole, deleteuser. for CUST package marking. please keep authentication logic as is using findbyusername. m
//TODO make sure people cant create 2 users with same name. make sure admins cant delete themselves.

public class UserDAO {

    public User findByUsername(String username) {
        String sql = "SELECT * FROM LocalUser WHERE username = ?";
        //query

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                //create new user object to pass back to loginservice
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}