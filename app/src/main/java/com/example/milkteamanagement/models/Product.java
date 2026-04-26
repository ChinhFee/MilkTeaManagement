package com.example.milkteamanagement.models; // Thay bằng package của bạn

public class Product {
    private String id;
    private String name;
    private int price;
    private String category;
    private String imageUrl;
    private boolean isAvailable;

    // BẮT BUỘC: Phải có Constructor trống để Firebase có thể đọc dữ liệu
    public Product() {}

    public Product(String id, String name, int price, String category, String imageUrl, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
    }

    // Các hàm Getter và Setter (Để đọc và ghi dữ liệu)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}