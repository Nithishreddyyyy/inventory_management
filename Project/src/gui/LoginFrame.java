package gui;

import db.UserDAO;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private UserDAO userDAO;
    public static User loggedInUser; // Store logged-in user globally

    public LoginFrame() {
        userDAO = new UserDAO();
        setTitle("Inventory Management - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15); gbc.gridx = 1; add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15); gbc.gridx = 1; add(passwordField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        loginButton.addActionListener(this::handleLogin);
        passwordField.addActionListener(this::handleLogin); // Login on Enter in password field
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userDAO.getUserByUsername(username);

        // DANGER: Plain text password comparison. Use hashing in real applications!
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = user;
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + user.getUsername(), "Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}