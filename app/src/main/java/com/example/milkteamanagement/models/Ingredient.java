package com.example.milkteamanagement.models;

public class Ingredient {
    private String id;
    private String name;
    private double quantity;
    private String unit; // g, ml, piece, etc.
    private double minThreshold; // When quantity < minThreshold, show red

    public Ingredient() {}

    public Ingredient(String id, String name, double quantity, String unit, double minThreshold) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.minThreshold = minThreshold;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getMinThreshold() { return minThreshold; }
    public void setMinThreshold(double minThreshold) { this.minThreshold = minThreshold; }

    public boolean isLowStock() {
        return quantity <= minThreshold;
    }
}
