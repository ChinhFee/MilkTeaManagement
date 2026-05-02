package com.example.milkteamanagement.models;

public class RecipeItem {
    private String materialId;
    private double quantity; // Lượng tiêu thụ cho 1 đơn vị (ly/phần)

    public RecipeItem() {}

    public RecipeItem(String materialId, double quantity) {
        this.materialId = materialId;
        this.quantity = quantity;
    }

    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}
