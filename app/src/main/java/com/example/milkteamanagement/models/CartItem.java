package com.example.milkteamanagement.models;

import java.util.List;

public class CartItem {
    private String productId;
    private String productName;
    private String productImageUrl;
    private int quantity;
    private String size;
    private String sugar;
    private String ice;
    private List<Topping> toppings;
    private double unitPrice;
    private double subTotal;

    public CartItem() {}

    public CartItem(String productId, String productName, String productImageUrl, int quantity, String size, String sugar, String ice, List<Topping> toppings, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity;
        this.size = size;
        this.sugar = sugar;
        this.ice = ice;
        this.toppings = toppings;
        this.unitPrice = unitPrice;
        calculateSubTotal();
    }

    public String getSugar() { return sugar; }
    public void setSugar(String sugar) { this.sugar = sugar; }

    public String getIce() { return ice; }
    public void setIce(String ice) { this.ice = ice; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public void calculateSubTotal() {
        double toppingPrice = 0;
        if (toppings != null) {
            for (Topping t : toppings) {
                toppingPrice += t.getPrice();
            }
        }
        
        double sizePrice = "L".equalsIgnoreCase(size) ? 5000 : 0;
        this.subTotal = (unitPrice + toppingPrice + sizePrice) * quantity;
    }

    public List<Topping> getToppings() {
        return toppings;
    }

    public void setToppings(List<Topping> toppings) {
        this.toppings = toppings;
        calculateSubTotal();
    }
    
    public double getSubTotal() {
        calculateSubTotal();
        return subTotal;
    }
    
    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}
