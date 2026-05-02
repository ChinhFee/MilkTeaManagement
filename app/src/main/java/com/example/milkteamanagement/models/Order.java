package com.example.milkteamanagement.models;

import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private List<CartItem> items;
    private double totalAmount;
    private String status;
    private String paymentMethod; 
    private long timestamp;
    private String note;
    private boolean materialsDeducted; // Cờ hiệu để biết đã trừ kho chưa

    public Order() {}

    public Order(String orderId, String customerId, String customerName, String customerPhone, String customerAddress, List<CartItem> items, double totalAmount, String status, String paymentMethod, long timestamp, String note) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.timestamp = timestamp;
        this.note = note;
        this.materialsDeducted = false;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isMaterialsDeducted() { return materialsDeducted; }
    public void setMaterialsDeducted(boolean materialsDeducted) { this.materialsDeducted = materialsDeducted; }
}
