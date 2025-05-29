package models;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String name;
    private String category;
    private BigDecimal price; // Use BigDecimal for currency
    private int quantity;     // Current stock level

    // Constructor for retrieving from DB
    public Product(int id, String name, String category, BigDecimal price, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    // Constructor for creating new products (DB assigns ID)
    public Product(String name, String category, BigDecimal price, int quantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() { // Useful for JComboBox
        return name + " (ID: " + id + ")";
    }
}