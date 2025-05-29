package gui;

import db.ProductDAO;
import models.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductManagementFrame extends JFrame {
    private ProductDAO productDAO;
    private JTable productsTable;
    private DefaultTableModel tableModel;

    private JTextField nameField, categoryField, priceField, quantityField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private Product selectedProduct = null;

    public ProductManagementFrame() {
        productDAO = new ProductDAO();
        setTitle("Manage Products");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Table Panel
        String[] columnNames = {"ID", "Name", "Category", "Price", "Quantity"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        productsTable = new JTable(tableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableRowClicked();
            }
        });
        add(new JScrollPane(productsTable), BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField(20); gbc.gridx = 1; gbc.gridwidth=3; formPanel.add(nameField, gbc); gbc.gridwidth=1;

        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Category:"), gbc);
        categoryField = new JTextField(15); gbc.gridx = 1; formPanel.add(categoryField, gbc);
        gbc.gridx = 2; gbc.gridy = y; formPanel.add(new JLabel("Price:"), gbc);
        priceField = new JTextField(10); gbc.gridx = 3; formPanel.add(priceField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Quantity:"), gbc);
        quantityField = new JTextField(5); gbc.gridx = 1; formPanel.add(quantityField, gbc);

        y++;
        JPanel buttonSubPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear Form");
        buttonSubPanel.add(addButton); buttonSubPanel.add(updateButton);
        buttonSubPanel.add(deleteButton); buttonSubPanel.add(clearButton);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(buttonSubPanel, gbc);

        add(formPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addOrUpdateProduct(true));
        updateButton.addActionListener(e -> addOrUpdateProduct(false));
        deleteButton.addActionListener(e -> deleteProduct());
        clearButton.addActionListener(e -> clearForm());

        loadProducts();
        enableFormButtons(true); // Add enabled, Update/Delete disabled
    }

    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            tableModel.addRow(new Object[]{
                    product.getId(), product.getName(), product.getCategory(),
                    product.getPrice(), product.getQuantity()
            });
        }
    }

    private void tableRowClicked() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
            selectedProduct = productDAO.getProductById(productId);
            if (selectedProduct != null) {
                nameField.setText(selectedProduct.getName());
                categoryField.setText(selectedProduct.getCategory());
                priceField.setText(selectedProduct.getPrice().toString());
                quantityField.setText(String.valueOf(selectedProduct.getQuantity()));
                enableFormButtons(false); // Add disabled, Update/Delete enabled
            }
        }
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty() || priceField.getText().trim().isEmpty() || quantityField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Price, and Quantity are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            new BigDecimal(priceField.getText().trim());
            Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a valid decimal and Quantity must be a valid integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void addOrUpdateProduct(boolean isAdding) {
        if (!validateInputs()) return;

        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        BigDecimal price = new BigDecimal(priceField.getText().trim());
        int quantity = Integer.parseInt(quantityField.getText().trim());

        if (isAdding) {
            Product product = new Product(name, category, price, quantity);
            if (productDAO.addProduct(product)) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else { // Updating
            if (selectedProduct == null) {
                JOptionPane.showMessageDialog(this, "Please select a product to update.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            selectedProduct.setName(name);
            selectedProduct.setCategory(category);
            selectedProduct.setPrice(price);
            selectedProduct.setQuantity(quantity);
            if (productDAO.updateProduct(selectedProduct)) {
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update product.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        loadProducts();
        clearForm();
    }


    private void deleteProduct() {
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete product: " + selectedProduct.getName() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (productDAO.deleteProduct(selectedProduct.getId())) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                loadProducts();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product. It might be referenced in stock transactions.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedProduct = null;
        nameField.setText(""); categoryField.setText("");
        priceField.setText(""); quantityField.setText("");
        productsTable.clearSelection();
        enableFormButtons(true);
    }

    private void enableFormButtons(boolean forAdding) {
        addButton.setEnabled(forAdding);
        updateButton.setEnabled(!forAdding);
        deleteButton.setEnabled(!forAdding);
    }
}