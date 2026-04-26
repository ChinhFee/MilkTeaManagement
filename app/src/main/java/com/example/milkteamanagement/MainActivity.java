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

    private static final String TAG = "FirestoreMenu";
    private MenuRepository menuRepository;
    private RecyclerView rcvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    
    // View báo lỗi
    private LinearLayout layoutStatus;
    private TextView tvStatusDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View
        rcvProducts = findViewById(R.id.rcvProducts);
        layoutStatus = findViewById(R.id.layoutStatus);
        tvStatusDetail = findViewById(R.id.tvStatusDetail);
        
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
                
                // KIỂM TRA LINK VÀ HIỂN THỊ LÊN GIAO DIỆN
                checkImageLinks(products);
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "Lỗi khi lấy dữ liệu: " + message);
            }
        });
    }

    private void checkImageLinks(List<Product> products) {
        int errorCount = 0;
        StringBuilder errorList = new StringBuilder();

        for (Product p : products) {
            String url = p.getImageUrl();
            if (url != null && url.contains("drive.google.com/drive/folders")) {
                errorCount++;
                errorList.append("- ").append(p.getName()).append("\n");
            }
        }

        if (errorCount > 0) {
            layoutStatus.setVisibility(View.VISIBLE);
            tvStatusDetail.setText("Có " + errorCount + " món đang dùng link thư mục (sai):\n" + errorList.toString());
        } else {
            layoutStatus.setVisibility(View.GONE);
        }
    }
}
