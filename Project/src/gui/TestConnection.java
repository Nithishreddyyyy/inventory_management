package gui;

import java.sql.*;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/inventory_db",
                    "root",
                    "test1234"
            );
            System.out.println("✅ Connected successfully!");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
