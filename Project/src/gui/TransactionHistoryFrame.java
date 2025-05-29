package gui;

import db.ProductDAO; // For product name lookup if needed, though DAO does it
import db.StockTransactionDAO;
import models.Product;
import models.StockTransaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionHistoryFrame extends JFrame {
    private StockTransactionDAO transactionDAO;
    private ProductDAO productDAO; // Used to populate filter and get product names
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private JComboBox<ProductComboItem> productFilterComboBox;
    private Map<Integer, String> productNameCache; // Cache for product names

    public TransactionHistoryFrame() {
        transactionDAO = new StockTransactionDAO();
        productDAO = new ProductDAO();
        productNameCache = new HashMap<>();

        setTitle("Stock Transaction History");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Product:"));
        productFilterComboBox = new JComboBox<>();
        JButton showAllButton = new JButton("Show All");
        filterPanel.add(productFilterComboBox);
        filterPanel.add(showAllButton);
        add(filterPanel, BorderLayout.NORTH);

        // Table Panel
        String[] columnNames = {"Trans. ID", "Product Name", "Type", "Quantity", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        transactionsTable = new JTable(tableModel);
        add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

        cacheProductNames();
        populateProductFilterComboBox();
        loadTransactions(null); // Load all initially

        productFilterComboBox.addActionListener(e -> {
            ProductComboItem selected = (ProductComboItem) productFilterComboBox.getSelectedItem();
            if (selected != null && selected.getProduct() != null) {
                loadTransactions(selected.getProduct().getId());
            } else if (productFilterComboBox.getSelectedIndex() == 0) { // "All Products" selected
                loadTransactions(null);
            }
        });

        showAllButton.addActionListener(e -> {
            productFilterComboBox.setSelectedIndex(0); // Ensure "All Products" is selected
            loadTransactions(null);
        });
    }

    private void cacheProductNames() {
        productNameCache.clear();
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            productNameCache.put(p.getId(), p.getName());
        }
    }

    private void populateProductFilterComboBox() {
        productFilterComboBox.removeAllItems();
        productFilterComboBox.addItem(new ProductComboItem(null)); // Represents "All Products"
        List<Product> products = productDAO.getAllProducts(); // Re-fetch or use cached if sure it's up-to-date
        for (Product product : products) {
            productFilterComboBox.addItem(new ProductComboItem(product));
        }
    }

    private String getCachedProductName(int productId) {
        return productNameCache.getOrDefault(productId, "Unknown Product (ID: " + productId + ")");
    }

    private void loadTransactions(Integer productIdFilter) {
        tableModel.setRowCount(0);
        List<StockTransaction> transactions;
        if (productIdFilter != null) {
            transactions = transactionDAO.getTransactionsByProductId(productIdFilter);
        } else {
            transactions = transactionDAO.getAllTransactions();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (StockTransaction st : transactions) {
            // The StockTransactionDAO's getAllTransactions and getByProductId now include product_name
            // So, we can retrieve it directly from the ResultSet mapping if we modify the DAO and mapRowToStockTransaction
            // For now, using the cache lookup method.
            // If your DAO's mapRowToStockTransaction directly puts product name into a field in StockTransaction (not ideal model-wise),
            // you could use st.getProductName() (if you added such a transient field).
            // The current StockTransactionDAO's mapRow... method doesn't add product_name to the ST object itself.
            // The JOIN is done, but the name is not mapped into the ST model object.
            // Let's assume we use the cache:
            String productName = getCachedProductName(st.getProductId());

            tableModel.addRow(new Object[]{
                    st.getId(),
                    productName, // Use cached name
                    st.getType(),
                    st.getQuantity(),
                    (st.getDate() != null) ? sdf.format(st.getDate()) : "N/A"
            });
        }
    }
}