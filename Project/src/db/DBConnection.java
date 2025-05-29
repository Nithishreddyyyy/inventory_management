package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // --- vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv ---
    // --- THIS IS WHERE YOU MAKE THE CHANGES ---
    // --- vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv ---
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db"; // MATCH THIS
    private static final String USER = "root";             // MATCH THIS
    private static final String PASSWORD = "test1234";       // MATCH THIS
    // --- ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ---
    // --- END OF CHANGES ---
    // --- ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ ---

    private static Connection connection = null;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // For MySQL Connector/J 8.x
            // For older versions (5.x), it might be "com.mysql.jdbc.Driver"
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                System.err.println("Connection Failed! Check output console and DB credentials in DBConnection.java");
                e.printStackTrace();
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
                connection = null; // Reset for next getConnection call
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Optional: Test connection using THIS class's configuration
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection(); // Uses the configured URL, USER, PASSWORD
            if (conn != null && !conn.isClosed()) {
                System.out.println("SUCCESS (via DBConnection.java): Connected to the database!");
                DBConnection.closeConnection();
                System.out.println("Connection (via DBConnection.java) closed.");
            } else {
                System.out.println("FAILURE (via DBConnection.java): Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.println("SQLException during DBConnection.java self-test:");
            e.printStackTrace();
        }
    }
}