package com.example.milkteamanagement.models;

import java.util.List;

public class Supply {
    private String supplyId;
    private long timestamp;
    private List<SupplyItem> items;
    private double totalCost;
    private String supplier;

    public Supply() {}

    public String getSupplyId() { return supplyId; }
    public void setSupplyId(String supplyId) { this.supplyId = supplyId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public List<SupplyItem> getItems() { return items; }
    public void setItems(List<SupplyItem> items) { this.items = items; }
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public static class SupplyItem {
        private String itemName;
        private int quantity;
        private double unitPrice;

        public SupplyItem() {}

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    }
}
