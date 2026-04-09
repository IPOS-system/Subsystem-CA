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
    //gets every user in the table
    //used by userspage
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM LocalUser ORDER BY user_id";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            System.out.println("//error getting all users: " + e.getMessage());
        }
        return users;
    }

    //stops duplicate usernames
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM LocalUser WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("//error checking username: " + e.getMessage());
            return false;
        }
    }

    //creates a new user
    public boolean createUser(String username, String password, String role) {
        if (usernameExists(username)) {
            return false;
        }
        String sql = "INSERT INTO LocalUser (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("//error creating user: " + e.getMessage());
            return false;
        }
    }

    //updates a users role
    public boolean updateUserRole(String username, String newRole) {
        String sql = "UPDATE LocalUser SET role = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRole);
            stmt.setString(2, username);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("//error updating role: " + e.getMessage());
            return false;
        }
    }

    //delets a user by username
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM LocalUser WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("//error deleting user: " + e.getMessage());
            return false;
        }
    }


}