package gui;

import db.ProductDAO;
import db.StockTransactionDAO;
import models.Product;
import models.StockTransaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class TransactionHistoryFrame extends JFrame {
    private StockTransactionDAO stockDAO;
    private ProductDAO productDAO;

    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    private JComboBox<ProductComboItem> productFilterCombo;
    private JComboBox<String> typeFilterCombo;
    private JButton filterButton, clearFilterButton, exportButton;

    public TransactionHistoryFrame() {
        stockDAO = new StockTransactionDAO();
        productDAO = new ProductDAO();
        initializeComponents();
        setupLayout();
        loadProducts();
        loadAllTransactions();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Transaction History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Filter components
        productFilterCombo = new JComboBox<>();
        typeFilterCombo = new JComboBox<>(new String[]{"All", "IN", "OUT"});

        filterButton = new JButton("Apply Filter");
        clearFilterButton = new JButton("Clear Filter");
        exportButton = new JButton("Export to CSV");

        filterButton.addActionListener(this::applyFilter);
        clearFilterButton.addActionListener(this::clearFilter);
        exportButton.addActionListener(this::exportToCsv);

        // History table
        String[] columns = {"ID", "Product", "Type", "Quantity", "Date"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Sort by date (most recent first)
        historyTable.setAutoCreateRowSorter(true);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel for filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        filterPanel.add(new JLabel("Product:"));
        filterPanel.add(productFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(10));

        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(10));

        filterPanel.add(filterButton);
        filterPanel.add(clearFilterButton);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(exportButton);

        // Center panel with table
        JScrollPane scrollPane = new JScrollPane(historyTable);

        // Bottom panel with summary
        JPanel summaryPanel = createSummaryPanel();

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));

        // This will be updated when transactions are loaded
        JLabel totalTransactionsLabel = new JLabel("Total Transactions: 0");
        JLabel totalInLabel = new JLabel("Total IN: 0");
        JLabel totalOutLabel = new JLabel("Total OUT: 0");

        panel.add(totalTransactionsLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(totalInLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(totalOutLabel);

        return panel;
    }

    private void loadProducts() {
        productFilterCombo.removeAllItems();
        productFilterCombo.addItem(new ProductComboItem(null, "All Products"));

        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            productFilterCombo.addItem(new ProductComboItem(product, null));
        }
    }

    private void loadAllTransactions() {
        historyTableModel.setRowCount(0);
        List<StockTransaction> transactions = stockDAO.getAllTransactions();

        for (StockTransaction transaction : transactions) {
            Object[] row = {
                    transaction.getId(),
                    transaction.getProductName(),
                    transaction.getType(),
                    transaction.getQuantity(),
                    transaction.getDate()
            };
            historyTableModel.addRow(row);
        }

        updateSummary(transactions);
    }

    private void applyFilter(ActionEvent e) {
        ProductComboItem selectedProduct = (ProductComboItem) productFilterCombo.getSelectedItem();
        String selectedType = (String) typeFilterCombo.getSelectedItem();

        historyTableModel.setRowCount(0);
        List<StockTransaction> transactions;

        if (selectedProduct != null && selectedProduct.getProduct() != null) {
            // Filter by specific product
            transactions = stockDAO.getTransactionsByProduct(selectedProduct.getProduct().getId());
        } else {
            // Show all transactions
            transactions = stockDAO.getAllTransactions();
        }

        // Apply type filter
        for (StockTransaction transaction : transactions) {
            if ("All".equals(selectedType) || selectedType.equals(transaction.getType())) {
                Object[] row = {
                        transaction.getId(),
                        transaction.getProductName(),
                        transaction.getType(),
                        transaction.getQuantity(),
                        transaction.getDate()
                };
                historyTableModel.addRow(row);
            }
        }

        updateSummary(transactions);
    }

    private void clearFilter(ActionEvent e) {
        productFilterCombo.setSelectedIndex(0);
        typeFilterCombo.setSelectedIndex(0);
        loadAllTransactions();
    }

    private void exportToCsv(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transaction History");
        fileChooser.setSelectedFile(new java.io.File("transaction_history.csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.io.PrintWriter writer = new java.io.PrintWriter(file);

                // Write header
                writer.println("ID,Product,Type,Quantity,Date");

                // Write data
                for (int i = 0; i < historyTableModel.getRowCount(); i++) {
                    StringBuilder line = new StringBuilder();
                    for (int j = 0; j < historyTableModel.getColumnCount(); j++) {
                        if (j > 0) line.append(",");
                        Object value = historyTableModel.getValueAt(i, j);
                        line.append(value != null ? value.toString() : "");
                    }
                    writer.println(line.toString());
                }

                writer.close();
                JOptionPane.showMessageDialog(this, "Transaction history exported successfully!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting file: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateSummary(List<StockTransaction> transactions) {
        int totalTransactions = transactions.size();
        int totalIn = 0;
        int totalOut = 0;

        for (StockTransaction transaction : transactions) {
            if ("IN".equals(transaction.getType())) {
                totalIn += transaction.getQuantity();
            } else if ("OUT".equals(transaction.getType())) {
                totalOut += transaction.getQuantity();
            }
        }

        // Update summary labels (you'll need to store references to these labels)
        Component summaryPanel = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (summaryPanel instanceof JPanel) {
            Component[] components = ((JPanel) summaryPanel).getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if (label.getText().startsWith("Total Transactions:")) {
                        label.setText("Total Transactions: " + totalTransactions);
                    } else if (label.getText().startsWith("Total IN:")) {
                        label.setText("Total IN: " + totalIn);
                    } else if (label.getText().startsWith("Total OUT:")) {
                        label.setText("Total OUT: " + totalOut);
                    }
                }
            }
        }
    }

    // Helper class for product combo box
    private static class ProductComboItem {
        private Product product;
        private String displayText;

        public ProductComboItem(Product product, String displayText) {
            this.product = product;
            this.displayText = displayText;
        }

        public Product getProduct() {
            return product;
        }

        @Override
        public String toString() {
            if (displayText != null) {
                return displayText;
            }
            return product.getName() + " (ID: " + product.getId() + ")";
        }
    }
}