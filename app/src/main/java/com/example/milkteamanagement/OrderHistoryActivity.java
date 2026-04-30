package com.example.milkteamanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.repositories.AuthRepository;
import com.example.milkteamanagement.repositories.OrderHistoryRepository;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrderHistory;
    private OrderHistoryAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llEmpty;
    private ProgressBar progressBar;
    
    private OrderHistoryRepository orderHistoryRepository;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        orderHistoryRepository = new OrderHistoryRepository();
        authRepository = AuthRepository.getInstance();

        initViews();
        setupRecyclerView();
        loadOrders();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadOrders);
    }

    private void initViews() {
        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        llEmpty = findViewById(R.id.llEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(new ArrayList<>());
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(adapter);
    }

    private void loadOrders() {
        String uid = authRepository.getCurrentUser().getUid();
        swipeRefresh.setRefreshing(true);
        
        orderHistoryRepository.fetchUserOrderHistory(uid, new OrderHistoryRepository.OrderCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                
                if (orders.isEmpty()) {
                    llEmpty.setVisibility(View.VISIBLE);
                    rvOrderHistory.setVisibility(View.GONE);
                } else {
                    llEmpty.setVisibility(View.GONE);
                    rvOrderHistory.setVisibility(View.VISIBLE);
                    adapter.updateList(orders);
                }
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderHistoryActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
