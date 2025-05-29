package gui;// package com.yourcompany; // If in a package

import gui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't set system look and feel.");
            }
            new LoginFrame().setVisible(true);
        });
    }
}