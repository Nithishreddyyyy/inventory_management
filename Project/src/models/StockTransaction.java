package models;

import java.sql.Timestamp;

public class StockTransaction {
    private int id;
    private int productId;
    private String productName; // For display purposes
    private String type; // "IN" or "OUT"
    private int quantity;
    private Timestamp date;
    private String reason; // Optional field for transaction reason

    // Constructors
    public StockTransaction() {}

    public StockTransaction(int productId, String type, int quantity) {
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
    }

    public StockTransaction(int productId, String type, int quantity, String reason) {
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.reason = reason;
    }

    public StockTransaction(int id, int productId, String productName, String type, int quantity, Timestamp date, String reason) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
        this.reason = reason;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public String toString() {
        return "StockTransaction{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", type='" + type + '\'' +
                ", quantity=" + quantity +
                ", date=" + date +
                ", reason='" + reason + '\'' +
                '}';
    }
}