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
import com.example.milkteamanagement.repositories.AuthRepository;
import com.example.milkteamanagement.repositories.CartManager;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.example.milkteamanagement.repositories.MenuRepository;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.example.milkteamanagement.models.ChatMessage;
import com.example.milkteamanagement.adapters.ChatAdapter;

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
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private ListenerRegistration orderStatusListener;
    private final Map<String, String> knownOrderStatuses = new HashMap<>();

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

        NotificationHelper.requestPostNotificationsIfNeeded(this);
        startOrderStatusNotifications();
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

        RecyclerView rvChat = view.findViewById(R.id.rvChatHistory);
        EditText etMessage = view.findViewById(R.id.etUserMessage);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        ProgressBar loading = view.findViewById(R.id.loadingAi);

        if (chatMessages.isEmpty()) {
            chatMessages.add(new ChatMessage("Chào bạn! Aura đã sẵn sàng hỗ trợ. Bạn muốn tìm món gì hôm nay?", ChatMessage.TYPE_AI));
        }

        chatAdapter = new ChatAdapter(chatMessages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);
        rvChat.scrollToPosition(chatMessages.size() - 1);

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (msg.isEmpty()) return;

            if (geminiRepository == null) {
                Toast.makeText(this, "Đang tải dữ liệu menu, vui lòng đợi...", Toast.LENGTH_SHORT).show();
                return;
            }

            chatMessages.add(new ChatMessage(msg, ChatMessage.TYPE_USER));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            rvChat.smoothScrollToPosition(chatMessages.size() - 1);
            
            loading.setVisibility(View.VISIBLE);
            etMessage.setText("");

            Futures.addCallback(geminiRepository.sendMessage(msg), new FutureCallback<com.google.ai.client.generativeai.type.GenerateContentResponse>() {
                @Override
                public void onSuccess(com.google.ai.client.generativeai.type.GenerateContentResponse result) {
                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        String responseText = formatAiResponse(result.getText());
                        chatMessages.add(new ChatMessage(responseText, ChatMessage.TYPE_AI));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        rvChat.smoothScrollToPosition(chatMessages.size() - 1);
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Gemini Error: " + t.getMessage(), t);
                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        String errorMsg = formatGeminiError(t);
                        chatMessages.add(new ChatMessage(errorMsg, ChatMessage.TYPE_AI));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        rvChat.smoothScrollToPosition(chatMessages.size() - 1);
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

                StringBuilder menuStr = new StringBuilder();
                for (Product p : fullProductList) {
                    menuStr.append(p.getName()).append(" (").append(p.getPrice()).append("đ), ");
                }

                if (geminiRepository == null) {
                    geminiRepository = new GeminiRepository(BuildConfig.GEMINI_API_KEY, menuStr.toString());
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
        
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        
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

    private void startOrderStatusNotifications() {
        if (AuthRepository.getInstance().getCurrentUser() == null) {
            return;
        }

        String uid = AuthRepository.getInstance().getCurrentUser().getUid();
        orderStatusListener = FirebaseFirestore.getInstance()
                .collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("customerId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        return;
                    }

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        String orderId = change.getDocument().getId();
                        String newStatus = change.getDocument().getString("status");
                        String oldStatus = knownOrderStatuses.put(orderId, newStatus);

                        if (change.getType() == DocumentChange.Type.MODIFIED
                                && oldStatus != null
                                && newStatus != null
                                && !newStatus.equals(oldStatus)) {
                            LocalNotificationSender.show(
                                    this,
                                    "Cap nhat don hang",
                                    getCustomerStatusMessage(newStatus)
                            );
                        }
                    }
                });
    }

    private String getCustomerStatusMessage(String status) {
        if (FirebaseConstants.STATUS_PROCESSING.equals(status)) return "Quan dang pha che don hang cua ban.";
        if (FirebaseConstants.STATUS_SHIPPED.equals(status)) return "Don hang cua ban dang duoc giao.";
        if (FirebaseConstants.STATUS_COMPLETED.equals(status)) return "Don hang cua ban da hoan thanh.";
        if (FirebaseConstants.STATUS_CANCELLED.equals(status)) return "Don hang cua ban da bi huy.";
        return "Trang thai don hang da duoc cap nhat: " + status;
    }

    @Override
    protected void onDestroy() {
        if (orderStatusListener != null) {
            orderStatusListener.remove();
            orderStatusListener = null;
        }
        super.onDestroy();
    }
}
