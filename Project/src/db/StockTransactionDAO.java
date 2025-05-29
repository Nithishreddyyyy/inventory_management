package db;

import models.StockTransaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockTransactionDAO {

    // Record a stock transaction and update product quantity
    public boolean recordTransaction(StockTransaction transaction) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert transaction record
            String insertSql = "INSERT INTO stock_transaction (product_id, type, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, transaction.getProductId());
                pstmt.setString(2, transaction.getType());
                pstmt.setInt(3, transaction.getQuantity());
                pstmt.executeUpdate();
            }

            // Update product quantity
            String updateSql;
            if ("IN".equals(transaction.getType())) {
                updateSql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
            } else {
                updateSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, transaction.getQuantity());
                pstmt.setInt(2, transaction.getProductId());
                int updated = pstmt.executeUpdate();

                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Check if quantity becomes negative
            String checkSql = "SELECT quantity FROM products WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setInt(1, transaction.getProductId());
                ResultSet rs = pstmt.executeQuery();

                if (rs.next() && rs.getInt("quantity") < 0) {
                    conn.rollback();
                    return false; // Don't allow negative stock
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Get all transactions with product names
    public List<StockTransaction> getAllTransactions() {
        List<StockTransaction> transactions = new ArrayList<>();
        String sql = """
            SELECT st.id, st.product_id, p.name as product_name, st.type, st.quantity, st.date
            FROM stock_transaction st
            JOIN products p ON st.product_id = p.id
            ORDER BY st.date DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                StockTransaction transaction = new StockTransaction(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("type"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("date"),
                        null
                );
                transactions.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    // Get transactions for a specific product
    public List<StockTransaction> getTransactionsByProduct(int productId) {
        List<StockTransaction> transactions = new ArrayList<>();
        String sql = """
            SELECT st.id, st.product_id, p.name as product_name, st.type, st.quantity, st.date
            FROM stock_transaction st
            JOIN products p ON st.product_id = p.id
            WHERE st.product_id = ?
            ORDER BY st.date DESC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                StockTransaction transaction = new StockTransaction(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("type"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("date"),
                        null
                );
                transactions.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    // Get recent transactions (last N records)
    public List<StockTransaction> getRecentTransactions(int limit) {
        List<StockTransaction> transactions = new ArrayList<>();
        String sql = """
            SELECT st.id, st.product_id, p.name as product_name, st.type, st.quantity, st.date
            FROM stock_transaction st
            JOIN products p ON st.product_id = p.id
            ORDER BY st.date DESC
            LIMIT ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                StockTransaction transaction = new StockTransaction(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("type"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("date"),
                        null
                );
                transactions.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    // Get products with low stock (below threshold)
    public List<models.Product> getLowStockProducts(int threshold) {
        List<models.Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE quantity <= ? ORDER BY quantity ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, threshold);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                models.Product product = new models.Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity")
                );
                products.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }
}