package gui;

import db.ProductDAO;
import models.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;

public class ProductManagementFrame extends JFrame {
    private ProductDAO productDAO;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // Form fields
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField priceField;
    private JTextField quantityField;

    private JButton addButton, updateButton, deleteButton, clearButton;
    private Product selectedProduct = null;

    public ProductManagementFrame() {
        productDAO = new ProductDAO();
        initializeComponents();
        setupLayout();
        loadProducts();
        setVisible(true);
    }

    private void initializeComponents() {
        setTitle("Product Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Initialize table
        String[] columnNames = {"ID", "Name", "Category", "Price", "Quantity"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add selection listener
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedProduct();
            }
        });

        // Initialize form fields
        nameField = new JTextField(20);
        categoryField = new JTextField(20);
        priceField = new JTextField(20);
        quantityField = new JTextField(20);

        // Initialize buttons
        addButton = new JButton("Add Product");
        updateButton = new JButton("Update Product");
        deleteButton = new JButton("Delete Product");
        clearButton = new JButton("Clear Form");

        // Search field
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        // Add action listeners
        addButton.addActionListener(this::addProduct);
        updateButton.addActionListener(this::updateProduct);
        deleteButton.addActionListener(this::deleteProduct);
        clearButton.addActionListener(this::clearForm);
        searchButton.addActionListener(this::searchProducts);

        // Initially disable update and delete buttons
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel for search
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JButton("Search") {{
            addActionListener(ProductManagementFrame.this::searchProducts);
        }});
        searchPanel.add(new JButton("Show All") {{
            addActionListener(e -> loadProducts());
        }});

        // Center panel with table
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        // Right panel for form
        JPanel formPanel = createFormPanel();

        // Add components
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        panel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        panel.add(quantityField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void loadProducts() {
        tableModel.setRowCount(0); // Clear existing data
        List<Product> products = productDAO.getAllProducts();

        for (Product product : products) {
            Object[] row = {
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getQuantity()
            };
            tableModel.addRow(row);
        }
    }

    private void loadSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (Integer) tableModel.getValueAt(selectedRow, 0);
            selectedProduct = productDAO.getProductById(productId);

            if (selectedProduct != null) {
                nameField.setText(selectedProduct.getName());
                categoryField.setText(selectedProduct.getCategory());
                priceField.setText(selectedProduct.getPrice().toString());
                quantityField.setText(String.valueOf(selectedProduct.getQuantity()));

                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                addButton.setEnabled(false);
            }
        }
    }

    private void addProduct(ActionEvent e) {
        if (validateForm()) {
            Product product = new Product(
                    nameField.getText().trim(),
                    categoryField.getText().trim(),
                    new BigDecimal(priceField.getText().trim()),
                    Integer.parseInt(quantityField.getText().trim())
            );

            if (productDAO.addProduct(product)) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                clearForm(null);
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product!");
            }
        }
    }

    private void updateProduct(ActionEvent e) {
        if (selectedProduct != null && validateForm()) {
            selectedProduct.setName(nameField.getText().trim());
            selectedProduct.setCategory(categoryField.getText().trim());
            selectedProduct.setPrice(new BigDecimal(priceField.getText().trim()));
            selectedProduct.setQuantity(Integer.parseInt(quantityField.getText().trim()));

            if (productDAO.updateProduct(selectedProduct)) {
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
                clearForm(null);
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update product!");
            }
        }
    }

    private void deleteProduct(ActionEvent e) {
        if (selectedProduct != null) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this product?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (productDAO.deleteProduct(selectedProduct.getId())) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                    clearForm(null);
                    loadProducts();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete product!");
                }
            }
        }
    }

    private void clearForm(ActionEvent e) {
        nameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        quantityField.setText("");

        selectedProduct = null;
        productTable.clearSelection();

        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void searchProducts(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            tableModel.setRowCount(0);
            List<Product> products = productDAO.searchProducts(searchTerm);

            for (Product product : products) {
                Object[] row = {
                        product.getId(),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getQuantity()
                };
                tableModel.addRow(row);
            }
        } else {
            loadProducts();
        }
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter product name!");
            return false;
        }

        if (categoryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter category!");
            return false;
        }

        try {
            new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid price!");
            return false;
        }

        try {
            Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid quantity!");
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductManagementFrame());
    }
}