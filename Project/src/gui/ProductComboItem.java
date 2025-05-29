package gui;

import models.Product;

public class ProductComboItem {
    private Product product;

    public ProductComboItem(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public String toString() {
        // Use product.getName() from the updated Product model
        return product != null ? product.getName() : "Select Product";
    }
}