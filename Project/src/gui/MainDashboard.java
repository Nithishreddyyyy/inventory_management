package gui;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {
    private JButton manageProductsButton;
    private JButton manageStockButton;
    private JButton viewTransactionsButton;
    private JButton logoutButton;

    public MainDashboard() {
        setTitle("Main Dashboard - Welcome " + (LoginFrame.loggedInUser != null ? LoginFrame.loggedInUser.getUsername() : "User"));
        setSize(600, 200); // Smaller as no low stock table
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15)); // Use GridLayout
        manageProductsButton = new JButton("Manage Products");
        manageStockButton = new JButton("Manage Stock");
        viewTransactionsButton = new JButton("View Transactions");
        logoutButton = new JButton("Logout");

        // Set preferred size for buttons to make them larger
        Dimension buttonSize = new Dimension(200, 50);
        manageProductsButton.setPreferredSize(buttonSize);
        manageStockButton.setPreferredSize(buttonSize);
        viewTransactionsButton.setPreferredSize(buttonSize);
        logoutButton.setPreferredSize(buttonSize);

        buttonPanel.add(manageProductsButton);
        buttonPanel.add(manageStockButton);
        buttonPanel.add(viewTransactionsButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(new JLabel("Inventory Management System", SwingConstants.CENTER), BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);

        manageProductsButton.addActionListener(e -> new ProductManagementFrame().setVisible(true));
        manageStockButton.addActionListener(e -> new StockManagementFrame().setVisible(true));
        viewTransactionsButton.addActionListener(e -> new TransactionHistoryFrame().setVisible(true));
        logoutButton.addActionListener(e -> {
            LoginFrame.loggedInUser = null;
            this.dispose();
            new LoginFrame().setVisible(true);
        });
    }
}