package gui;

import db.ProductDAO;
import db.StockTransactionDAO;
import models.Product;
import models.StockTransaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StockManagementFrame extends JFrame {
    private ProductDAO productDAO;
    private StockTransactionDAO transactionDAO;

    private JComboBox<ProductComboItem> productComboBox;
    private JTextField quantityField;
    private JRadioButton stockInRadio, stockOutRadio;
    private JButton processButton;

    private JTable stockLevelsTable;
    private DefaultTableModel stockLevelsTableModel;

    public StockManagementFrame() {
        productDAO = new ProductDAO();
        transactionDAO = new StockTransactionDAO();

        setTitle("Manage Stock");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        // Transaction Panel
        JPanel transactionPanel = new JPanel(new GridBagLayout());
        transactionPanel.setBorder(BorderFactory.createTitledBorder("Process Stock Transaction"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; transactionPanel.add(new JLabel("Product:"), gbc);
        productComboBox = new JComboBox<>(); gbc.gridx = 1; gbc.gridwidth=2; transactionPanel.add(productComboBox, gbc); gbc.gridwidth=1;

        y++;
        gbc.gridx = 0; gbc.gridy = y; transactionPanel.add(new JLabel("Quantity:"), gbc);
        quantityField = new JTextField(5); gbc.gridx = 1; transactionPanel.add(quantityField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; transactionPanel.add(new JLabel("Type:"), gbc);
        stockInRadio = new JRadioButton("Stock In"); stockInRadio.setSelected(true);
        stockOutRadio = new JRadioButton("Stock Out");
        ButtonGroup group = new ButtonGroup(); group.add(stockInRadio); group.add(stockOutRadio);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(stockInRadio); radioPanel.add(stockOutRadio);
        gbc.gridx = 1; gbc.gridwidth = 2; transactionPanel.add(radioPanel, gbc); gbc.gridwidth = 1;

        y++;
        processButton = new JButton("Process Transaction");
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.CENTER;
        transactionPanel.add(processButton, gbc);
        add(transactionPanel, BorderLayout.NORTH);

        // Stock Levels Table Panel
        JPanel stockLevelsPanel = new JPanel(new BorderLayout());
        stockLevelsPanel.setBorder(BorderFactory.createTitledBorder("Current Product Stock"));
        String[] columnNames = {"ID", "Product Name", "Current Quantity"};
        stockLevelsTableModel = new DefaultTableModel(columnNames, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        stockLevelsTable = new JTable(stockLevelsTableModel);
        stockLevelsPanel.add(new JScrollPane(stockLevelsTable), BorderLayout.CENTER);
        add(stockLevelsPanel, BorderLayout.CENTER);

        loadProductsIntoComboBox();
        loadStockLevels();
        processButton.addActionListener(e -> processTransaction());
    }

    private void loadProductsIntoComboBox() {
        productComboBox.removeAllItems();
        productComboBox.addItem(new ProductComboItem(null)); // "Select Product"
        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            productComboBox.addItem(new ProductComboItem(product));
        }
    }

    private void loadStockLevels() {
        stockLevelsTableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            stockLevelsTableModel.addRow(new Object[]{
                    product.getId(), product.getName(), product.getQuantity()
            });
        }
    }

    private boolean validateTransaction() {
        if (productComboBox.getSelectedItem() == null || ((ProductComboItem) productComboBox.getSelectedItem()).getProduct() == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (quantityField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Quantity is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void processTransaction() {
        if (!validateTransaction()) return;

        ProductComboItem selectedItem = (ProductComboItem) productComboBox.getSelectedItem();
        Product selectedProduct = selectedItem.getProduct();
        int transactionQuantity = Integer.parseInt(quantityField.getText().trim());
        String type = stockInRadio.isSelected() ? "IN" : "OUT";

        if ("OUT".equals(type) && transactionQuantity > selectedProduct.getQuantity()) {
            JOptionPane.showMessageDialog(this, "Cannot stock out more than available. Current stock: " + selectedProduct.getQuantity(), "Stock Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Create StockTransaction record
        StockTransaction transaction = new StockTransaction(selectedProduct.getId(), type, transactionQuantity);
        boolean transactionLogged = transactionDAO.addTransaction(transaction);

        if (transactionLogged) {
            // 2. Update product's quantity in products table
            int newProductQuantity = selectedProduct.getQuantity();
            if ("IN".equals(type)) {
                newProductQuantity += transactionQuantity;
            } else { // OUT
                newProductQuantity -= transactionQuantity;
            }
            selectedProduct.setQuantity(newProductQuantity);
            boolean productUpdated = productDAO.updateProduct(selectedProduct);

            if (productUpdated) {
                JOptionPane.showMessageDialog(this, "Transaction processed successfully!");
                loadStockLevels(); // Refresh table in this frame
                loadProductsIntoComboBox(); // Refresh combo box (product quantity might affect decisions)
                quantityField.setText("");
                productComboBox.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(this, "Transaction logged, but failed to update product stock. Please check manually.", "Partial Error", JOptionPane.ERROR_MESSAGE);
                // Consider rolling back the stock_transaction or other error recovery
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to log stock transaction.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}