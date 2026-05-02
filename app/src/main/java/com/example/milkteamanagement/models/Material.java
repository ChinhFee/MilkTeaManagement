package com.example.milkteamanagement.models;

public class Material {
    private String id;
    private String name;
    private double quantity;
    private String unit; // g, ml, piece, etc.
    private double minThreshold; // When quantity < minThreshold, show red
    private String imageUrl;

    public Material() {}

    public Material(String id, String name, double quantity, String unit, double minThreshold) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.minThreshold = minThreshold;
    }

    public Material(String id, String name, double quantity, String unit, double minThreshold, String imageUrl) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.minThreshold = minThreshold;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isLowStock() {
        return quantity <= minThreshold;
    }
}
