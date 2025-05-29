package db;

import models.StockTransaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDAO {

    public boolean addTransaction(StockTransaction transaction) {
        // The 'date' column has a DEFAULT CURRENT_TIMESTAMP, so we don't need to set it explicitly unless overriding.
        String sql = "INSERT INTO stock_transaction (product_id, type, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, transaction.getProductId());
            pstmt.setString(2, transaction.getType());
            pstmt.setInt(3, transaction.getQuantity());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setId(generatedKeys.getInt(1));
                        // Optionally, retrieve the timestamp set by the database
                        try (PreparedStatement tsPstmt = conn.prepareStatement("SELECT date FROM stock_transaction WHERE id = ?")) {
                            tsPstmt.setInt(1, transaction.getId());
                            ResultSet tsRs = tsPstmt.executeQuery();
                            if (tsRs.next()) {
                                transaction.setDate(tsRs.getTimestamp("date"));
                            }
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding stock transaction for product ID: " + transaction.getProductId());
            e.printStackTrace();
        }
        return false;
    }

    public List<StockTransaction> getAllTransactions() {
        List<StockTransaction> transactions = new ArrayList<>();
        // We'll need product name for display, so a JOIN is good here.
        String sql = "SELECT st.id, st.product_id, p.name as product_name, st.type, st.quantity, st.date " +
                "FROM stock_transaction st " +
                "JOIN products p ON st.product_id = p.id " +
                "ORDER BY st.date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(mapRowToStockTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all stock transactions.");
            e.printStackTrace();
        }
        return transactions;
    }

    public List<StockTransaction> getTransactionsByProductId(int productId) {
        List<StockTransaction> transactions = new ArrayList<>();
        String sql = "SELECT st.id, st.product_id, p.name as product_name, st.type, st.quantity, st.date " +
                "FROM stock_transaction st " +
                "JOIN products p ON st.product_id = p.id " +
                "WHERE st.product_id = ? ORDER BY st.date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(mapRowToStockTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions for product ID: " + productId);
            e.printStackTrace();
        }
        return transactions;
    }

    // This helper expects 'product_name' to be in the ResultSet (from a JOIN)
    // If not joining, you'd need another lookup for product_name.
    private StockTransaction mapRowToStockTransaction(ResultSet rs) throws SQLException {
        // This constructor is for display primarily, so it expects product_name.
        // The StockTransaction model itself doesn't store product_name directly.
        StockTransaction st = new StockTransaction(
                rs.getInt("id"),
                rs.getInt("product_id"),
                rs.getString("type"),
                rs.getInt("quantity"),
                rs.getTimestamp("date")
        );
        return st; // The `product_name` from `rs.getString("product_name")` will be used in the Frame.
    }
}