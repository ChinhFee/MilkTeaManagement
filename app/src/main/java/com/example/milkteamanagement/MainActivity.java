package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.repositories.CartManager;
import com.example.milkteamanagement.repositories.MenuRepository;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MenuRepository menuRepository;
    private RecyclerView rcvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private android.widget.ImageView btnProfile;
    private ExtendedFloatingActionButton fabCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View
        rcvProducts = findViewById(R.id.rcvProducts);
        btnProfile = findViewById(R.id.btnProfile);
        fabCart = findViewById(R.id.fabCart);
        rcvProducts.setLayoutManager(new LinearLayoutManager(this));

        // Sự kiện nút Profile
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        // Sự kiện nút Giỏ hàng
        fabCart.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CartActivity.class));
        });

        // Khởi tạo danh sách và Adapter
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        rcvProducts.setAdapter(productAdapter);

        // Khởi tạo Repository
        menuRepository = new MenuRepository();

        // Lấy dữ liệu từ Firebase
        loadMenuFromFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartFab();
    }

    private void updateCartFab() {
        int count = CartManager.getInstance().getCartItems().size();
        if (count > 0) {
            fabCart.setText("Xem giỏ hàng (" + count + ")");
            fabCart.show();
        } else {
            fabCart.hide();
        }
    }

    private void loadMenuFromFirebase() {
        menuRepository.getProductsRealtime(new MenuRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                productList.clear();
                productList.addAll(products);
                productAdapter.notifyDataSetChanged();
                Log.d(TAG, "Lấy dữ liệu thành công, số lượng món: " + products.size());
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "Lỗi khi lấy dữ liệu: " + message);
            }
        });
    }
}
