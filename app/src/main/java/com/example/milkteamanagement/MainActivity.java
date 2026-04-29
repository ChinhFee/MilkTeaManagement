package com.example.milkteamanagement;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.repositories.MenuRepository;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MenuRepository menuRepository;
    private RecyclerView rcvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    
    // View báo lỗi (Hỗ trợ kiểm tra link ảnh khi triển khai)
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View
        rcvProducts = findViewById(R.id.rcvProducts);
        
        rcvProducts.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        rcvProducts.setAdapter(productAdapter);

        // Khởi tạo Repository
        menuRepository = new MenuRepository();

        // Lấy dữ liệu từ Firebase và hiển thị lên màn hình
        loadMenuFromFirebase();
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
