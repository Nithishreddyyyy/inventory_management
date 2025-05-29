package db;

import models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"), // Plain text password from DB
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by username: " + username);
            e.printStackTrace();
        }
        return null;
    }

    // Add more user-related methods if needed (e.g., addUser, updateUserPassword)
}