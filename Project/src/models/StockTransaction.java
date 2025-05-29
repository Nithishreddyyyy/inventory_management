package models;

import java.sql.Timestamp; // Or java.time.LocalDateTime

public class StockTransaction {
    private int id;
    private int productId;
    private String type; // "IN", "OUT"
    private int quantity;
    private Timestamp date; // Or LocalDateTime

    // Constructor for retrieving from DB
    public StockTransaction(int id, int productId, String type, int quantity, Timestamp date) {
        this.id = id;
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
    }

    // Constructor for creating new transaction (DB assigns ID and date)
    public StockTransaction(int productId, String type, int quantity) {
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    @Override
    public String toString() {
        return "Transaction [ID=" + id + ", ProductID=" + productId + ", Type=" + type + ", Quantity=" + quantity + ", Date=" + date + "]";
    }
}