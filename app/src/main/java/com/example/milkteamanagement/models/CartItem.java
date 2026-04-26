package com.example.milkteamanagement.models;

import java.util.List;

public class CartItem {
    private String productId;
    private String productName;
    private int quantity;
    private String size; // M, L
    private List<Topping> toppings; // Đổi từ String sang Model Topping
    private double unitPrice; // Giá gốc của 1 ly (chưa topping)
    private double subTotal; // Tổng giá sau khi cộng topping và nhân số lượng

    public CartItem() {}

    public CartItem(String productId, String productName, int quantity, String size, List<Topping> toppings, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.size = size;
        this.toppings = toppings;
        this.unitPrice = unitPrice;
        calculateSubTotal();
    }

    public void calculateSubTotal() {
        double toppingPrice = 0;
        if (toppings != null) {
            for (Topping t : toppings) {
                toppingPrice += t.getPrice();
            }
        }
        
        // Giả sử size L thêm 5000đ (Bạn có thể tùy chỉnh logic này)
        double sizePrice = "L".equalsIgnoreCase(size) ? 5000 : 0;
        
        this.subTotal = (unitPrice + toppingPrice + sizePrice) * quantity;
    }

    // Getter/Setter cho toppings đã đổi kiểu dữ liệu
    public List<Topping> getToppings() {
        return toppings;
    }

    public void setToppings(List<Topping> toppings) {
        this.toppings = toppings;
        calculateSubTotal();
    }
    
    public double getSubTotal() {
        calculateSubTotal(); // Luôn tính lại trước khi lấy giá
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
