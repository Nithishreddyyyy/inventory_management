package gui;

import db.ProductDAO;
import db.StockTransactionDAO;
import models.Product;
import models.StockTransaction;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class StockManagementFrame extends JFrame {
    private ProductDAO productDAO;
    private StockTransactionDAO stockDAO;

    // Product selection
    private JComboBox<ProductComboItem> productCombo;
    private JLabel currentStockLabel;

    // Transaction input
    private JComboBox<String> transactionTypeCombo;
    private JTextField quantityField;
    private JTextArea reasonArea;

    // Buttons
    private JButton recordButton, refreshButton, viewHistoryButton;

    // Transaction history table
    private JTable transactionTable;
    private DefaultTableModel transactionTableModel;

    // Low stock alerts
    private JTable lowStockTable;
    private DefaultTableModel lowStockTableModel;

    public StockManagementFrame() {
        productDAO = new ProductDAO();
        stockDAO = new StockTransactionDAO();
        initializeComponents();
        setupLayout();
        loadProducts();
        loadRecentTransactions();
        checkLowStock();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Stock Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Product selection
        productCombo = new JComboBox<>();
        productCombo.addActionListener(this::updateCurrentStock);
        currentStockLabel = new JLabel("Current Stock: 0");

        // Transaction type
        transactionTypeCombo = new JComboBox<>(new String[]{"IN", "OUT"});
        quantityField = new JTextField(10);
        reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);

        // Buttons
        recordButton = new JButton("Record Transaction");
        refreshButton = new JButton("Refresh");
        viewHistoryButton = new JButton("View Full History");

        recordButton.addActionListener(this::recordTransaction);
        refreshButton.addActionListener(this::refreshData);
        viewHistoryButton.addActionListener(this::viewFullHistory);

        // Transaction history table
        String[] transactionColumns = {"ID", "Product", "Type", "Quantity", "Date"};
        transactionTableModel = new DefaultTableModel(transactionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(transactionTableModel);

        // Low stock table
        String[] lowStockColumns = {"ID", "Product", "Category", "Current Stock", "Price"};
        lowStockTableModel = new DefaultTableModel(lowStockColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lowStockTable = new JTable(lowStockTableModel);
        lowStockTable.getColumnModel().getColumn(3).setCellRenderer(new LowStockCellRenderer());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel for transaction input
        JPanel inputPanel = createInputPanel();

        // Center panel with tables
        JTabbedPane tabbedPane = new JTabbedPane();

        // Recent transactions tab
        JScrollPane transactionScrollPane = new JScrollPane(transactionTable);
        transactionScrollPane.setPreferredSize(new Dimension(0, 200));
        tabbedPane.addTab("Recent Transactions", transactionScrollPane);

        // Low stock alerts tab
        JScrollPane lowStockScrollPane = new JScrollPane(lowStockTable);
        lowStockScrollPane.setPreferredSize(new Dimension(0, 200));
        tabbedPane.addTab("Low Stock Alerts", lowStockScrollPane);

        // Bottom panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewHistoryButton);

        add(inputPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Record Stock Transaction"));

        // Left side - Product selection
        JPanel productPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        productPanel.add(new JLabel("Select Product:"), gbc);
        gbc.gridx = 1;
        productPanel.add(productCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        productPanel.add(new JLabel("Current Stock:"), gbc);
        gbc.gridx = 1;
        productPanel.add(currentStockLabel, gbc);

        // Center - Transaction details
        JPanel transactionPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0; gbc.gridy = 0;
        transactionPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        transactionPanel.add(transactionTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        transactionPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        transactionPanel.add(quantityField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        transactionPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        transactionPanel.add(new JScrollPane(reasonArea), gbc);

        // Right side - Action button
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.add(recordButton);

        panel.add(productPanel, BorderLayout.WEST);
        panel.add(transactionPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private void loadProducts() {
        productCombo.removeAllItems();
        List<Product> products = productDAO.getAllProducts();

        for (Product product : products) {
            productCombo.addItem(new ProductComboItem(product));
        }

        if (productCombo.getItemCount() > 0) {
            updateCurrentStock(null);
        }
    }

    private void updateCurrentStock(ActionEvent e) {
        ProductComboItem selected = (ProductComboItem) productCombo.getSelectedItem();
        if (selected != null) {
            currentStockLabel.setText("Current Stock: " + selected.getProduct().getQuantity());
        }
    }

    private void recordTransaction(ActionEvent e) {
        if (validateTransactionInput()) {
            ProductComboItem selected = (ProductComboItem) productCombo.getSelectedItem();
            String type = (String) transactionTypeCombo.getSelectedItem();
            int quantity = Integer.parseInt(quantityField.getText().trim());

            StockTransaction transaction = new StockTransaction(
                    selected.getProduct().getId(),
                    type,
                    quantity,
                    reasonArea.getText().trim()
            );

            if (stockDAO.recordTransaction(transaction)) {
                JOptionPane.showMessageDialog(this, "Transaction recorded successfully!");
                clearTransactionForm();
                refreshData(null);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to record transaction!\nCheck if sufficient stock is available for OUT transactions.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshData(ActionEvent e) {
        loadProducts();
        loadRecentTransactions();
        checkLowStock();
    }

    private void loadRecentTransactions() {
        transactionTableModel.setRowCount(0);
        List<StockTransaction> transactions = stockDAO.getRecentTransactions(20);

        for (StockTransaction transaction : transactions) {
            Object[] row = {
                    transaction.getId(),
                    transaction.getProductName(),
                    transaction.getType(),
                    transaction.getQuantity(),
                    transaction.getDate()
            };
            transactionTableModel.addRow(row);
        }
    }

    private void checkLowStock() {
        lowStockTableModel.setRowCount(0);
        List<Product> lowStockProducts = stockDAO.getLowStockProducts(10); // Threshold: 10

        for (Product product : lowStockProducts) {
            Object[] row = {
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getQuantity(),
                    product.getPrice()
            };
            lowStockTableModel.addRow(row);
        }
    }

    private void viewFullHistory(ActionEvent e) {
        new TransactionHistoryFrame();
    }

    private boolean validateTransactionInput() {
        if (productCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a product!");
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!");
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity!");
            return false;
        }

        return true;
    }

    private void clearTransactionForm() {
        quantityField.setText("");
        reasonArea.setText("");
        transactionTypeCombo.setSelectedIndex(0);
    }

    // Helper class for product combo box
    private static class ProductComboItem {
        private Product product;

        public ProductComboItem(Product product) {
            this.product = product;
        }

        public Product getProduct() {
            return product;
        }

        @Override
        public String toString() {
            return product.getName() + " (ID: " + product.getId() + ")";
        }
    }

    // Custom cell renderer for low stock highlighting
    private static class LowStockCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected && column == 3) { // Quantity column
                int quantity = (Integer) value;
                if (quantity <= 5) {
                    c.setBackground(Color.RED);
                    c.setForeground(Color.WHITE);
                } else if (quantity <= 10) {
                    c.setBackground(Color.ORANGE);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            } else if (!isSelected) {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StockManagementFrame());
    }
}