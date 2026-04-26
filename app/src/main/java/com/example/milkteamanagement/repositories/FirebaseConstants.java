package com.example.milkteamanagement.repositories;

public class FirebaseConstants {
    // Collections
    public static final String COL_PRODUCTS = "Products";
    public static final String COL_ORDERS = "Orders";
    public static final String COL_USERS = "Users";
    public static final String COL_TOPPINGS = "Toppings";

    // Order Statuses
    public static final String STATUS_PENDING = "Đang chờ";
    public static final String STATUS_PROCESSING = "Đang pha chế";
    public static final String STATUS_SHIPPED = "Đang giao";
    public static final String STATUS_COMPLETED = "Đã hoàn thành";
    public static final String STATUS_CANCELLED = "Đã hủy";

    // Payment Methods
    public static final String PAYMENT_CASH = "Tiền mặt";
    public static final String PAYMENT_BANK_TRANSFER = "Chuyển khoản";

    // User Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
}
