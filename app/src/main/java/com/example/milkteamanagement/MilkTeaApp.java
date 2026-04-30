package com.example.milkteamanagement;

import android.app.Application;
import com.example.milkteamanagement.repositories.CartManager;

public class MilkTeaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo CartManager ngay khi app bắt đầu để đảm bảo dữ liệu được load từ SharedPreferences
        CartManager.getInstance(this);
    }
}
