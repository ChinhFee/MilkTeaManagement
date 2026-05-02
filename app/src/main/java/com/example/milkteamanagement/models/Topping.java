package com.example.milkteamanagement.models;

import java.util.List;

public class Topping {
    private String id;
    private String name;
    private int price;
    private boolean isAvailable;
    private List<RecipeItem> recipe;

    public Topping() {
    }

    public Topping(String id, String name, int price, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<RecipeItem> getRecipe() {
        return recipe;
    }

    public void setRecipe(List<RecipeItem> recipe) {
        this.recipe = recipe;
    }
}
