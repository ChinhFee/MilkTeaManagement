package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.repositories.CartManager;
import com.example.milkteamanagement.repositories.MenuRepository;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MenuRepository menuRepository;
    private RecyclerView rcvProducts;
    private ProductAdapter productAdapter;
    private final List<Object> displayList = new ArrayList<>();
    private final List<Product> fullProductList = new ArrayList<>();
    private ImageView btnProfile;
    private ExtendedFloatingActionButton fabCart;
    private TabLayout tabLayout;
    private EditText etSearch;
    private String currentCategory = "Tất cả";
    private boolean isUpdatingTabs = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        
        productAdapter = new ProductAdapter(displayList);
        rcvProducts.setAdapter(productAdapter);
        
        menuRepository = new MenuRepository();
        loadMenuFromFirebase();
    }

    private void initViews() {
        rcvProducts = findViewById(R.id.rcvProducts);
        btnProfile = findViewById(R.id.btnProfile);
        fabCart = findViewById(R.id.fabCart);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.etSearch);
        rcvProducts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProduct(currentCategory, s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        fabCart.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
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
                if (products == null) return;
                
                fullProductList.clear();
                fullProductList.addAll(products);
                
                setupTabs(products);
                
                String searchQuery = etSearch.getText() != null ? etSearch.getText().toString() : "";
                filterProduct(currentCategory, searchQuery);
            }

            @Override
            public void onFailure(String message) {
                Log.e(TAG, "Lỗi Firebase: " + message);
            }
        });
    }

    private void setupTabs(List<Product> products) {
        isUpdatingTabs = true;
        
        String savedCategory = currentCategory;
        tabLayout.removeAllTabs();
        
        // Thêm tab "Tất cả"
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        
        // Lấy danh sách category duy nhất và sắp xếp A-Z
        Set<String> categories = new TreeSet<>();
        for (Product p : products) {
            if (p != null && p.getCategory() != null && !p.getCategory().trim().isEmpty()) {
                categories.add(p.getCategory().trim());
            }
        }
        
        int selectedIndex = 0;
        int count = 1;
        for (String category : categories) {
            TabLayout.Tab tab = tabLayout.newTab().setText(category);
            tabLayout.addTab(tab);
            if (category.equals(savedCategory)) {
                selectedIndex = count;
            }
            count++;
        }
        
        // Chọn lại tab cũ nếu nó vẫn tồn tại
        TabLayout.Tab tabToSelect = tabLayout.getTabAt(selectedIndex);
        if (tabToSelect != null) {
            tabToSelect.select();
        }

        tabLayout.clearOnTabSelectedListeners();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!isUpdatingTabs && tab.getText() != null) {
                    currentCategory = tab.getText().toString();
                    filterProduct(currentCategory, etSearch.getText().toString());
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        isUpdatingTabs = false;
    }

    private void filterProduct(String category, String query) {
        displayList.clear();
        String safeQuery = (query == null) ? "" : query.toLowerCase().trim();
        
        Map<String, List<Product>> groupedProducts = new TreeMap<>();
        
        for (Product p : fullProductList) {
            if (p == null) continue;

            String name = p.getName() != null ? p.getName() : "Không tên";
            String cat = (p.getCategory() != null && !p.getCategory().isEmpty()) ? p.getCategory() : "Khác";

            boolean matchesCategory = category.equals("Tất cả") || cat.equals(category);
            boolean matchesSearch = name.toLowerCase().contains(safeQuery);
            
            if (matchesCategory && matchesSearch) {
                if (!groupedProducts.containsKey(cat)) {
                    groupedProducts.put(cat, new ArrayList<>());
                }
                groupedProducts.get(cat).add(p);
            }
        }

        for (Map.Entry<String, List<Product>> entry : groupedProducts.entrySet()) {
            displayList.add(entry.getKey());
            displayList.addAll(entry.getValue());
        }
        
        if (productAdapter != null) {
            runOnUiThread(() -> productAdapter.notifyDataSetChanged());
        }
    }
}
