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

import com.example.milkteamanagement.repositories.GeminiRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private GeminiRepository geminiRepository;

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
        findViewById(R.id.fabAi).setOnClickListener(v -> showAiDialog());
    }

    private void showAiDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_aura_ai, null);
        dialog.setContentView(view);

        TextView tvResponse = view.findViewById(R.id.tvAiResponse);
        EditText etMessage = view.findViewById(R.id.etUserMessage);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        ProgressBar loading = view.findViewById(R.id.loadingAi);

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (msg.isEmpty()) return;

            if (geminiRepository == null) {
                Toast.makeText(this, "Đang tải dữ liệu menu, vui lòng đợi...", Toast.LENGTH_SHORT).show();
                return;
            }

            loading.setVisibility(View.VISIBLE);
            tvResponse.setText("Aura đang suy nghĩ...");
            etMessage.setText("");

            Futures.addCallback(geminiRepository.sendMessage(msg), new FutureCallback<com.google.ai.client.generativeai.type.GenerateContentResponse>() {
                @Override
                public void onSuccess(com.google.ai.client.generativeai.type.GenerateContentResponse result) {
                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        tvResponse.setText(formatAiResponse(result.getText()));
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Gemini Error: " + t.getMessage(), t);
                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        tvResponse.setText(formatGeminiError(t));
                    });
                }
            }, androidx.core.content.ContextCompat.getMainExecutor(MainActivity.this));
        });

        dialog.show();
    }

    private String formatAiResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "AuraAI chưa có phản hồi. Bạn thử hỏi lại ngắn gọn hơn nhé.";
        }

        String formatted = response.trim()
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("(?m)^\\s*\\*\\s+", "- ")
                .replaceAll("(?m)^\\s*-\\s*\\*\\*(.*?)\\*\\*\\s*:", "- $1:")
                .replaceAll("(?m)^\\s*#{1,6}\\s*", "")
                .replace("*", "")
                .replaceAll("\\n{3,}", "\n\n");

        return formatted.trim();
    }

    private String formatGeminiError(Throwable throwable) {
        String detail = throwable != null ? throwable.toString() : "";
        String normalized = detail.toLowerCase();

        if (normalized.contains("404") || normalized.contains("not_found") || normalized.contains("not found")) {
            return "AuraAI chưa gọi được model Gemini. Hãy dùng model đang còn hỗ trợ, ví dụ gemini-2.5-flash, rồi build lại app.";
        }

        if (normalized.contains("403") || normalized.contains("permission") || normalized.contains("forbidden")
                || normalized.contains("api key")) {
            return "API key đang bị từ chối. Hãy kiểm tra API key có được tạo trong Google AI Studio/Gemini API, đã bật Gemini API, và phần Application restrictions có đúng package com.example.milkteamanagement + SHA-1 của máy build không.";
        }

        if (normalized.contains("429") || normalized.contains("quota") || normalized.contains("rate")) {
            return "AuraAI đang vượt quota hoặc rate limit. Hãy chờ một lúc rồi thử lại, hoặc kiểm tra quota/billing của Gemini API.";
        }

        if (normalized.contains("timeout") || normalized.contains("deadline") || normalized.contains("unable to resolve host")) {
            return "Không kết nối được Gemini API. Hãy kiểm tra mạng trên thiết bị/emulator rồi thử lại.";
        }

        return "AuraAI đang gặp lỗi khi gọi Gemini. Xem Logcat tag MainActivity để biết chi tiết.";
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

                // Tạo chuỗi menu một lần duy nhất khi dữ liệu thay đổi
                StringBuilder menuStr = new StringBuilder();
                for (Product p : fullProductList) {
                    menuStr.append(p.getName()).append(" (").append(p.getPrice()).append("đ), ");
                }

                // Khởi tạo hoặc cập nhật Gemini với menu mới
                if (geminiRepository == null) {
                    geminiRepository = new GeminiRepository("AIzaSyB8HK3Zk6gGOAtO_c1-QsJ0sW2qj6Er0qs", menuStr.toString());
                }
                
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
