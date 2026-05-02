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
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.example.milkteamanagement.repositories.OrderHistoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderTrackingActivity extends AppCompatActivity {

    private RecyclerView rvOrderTracking;
    private OrderHistoryAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout llEmpty;
    private ProgressBar progressBar;

    private OrderHistoryRepository orderHistoryRepository;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderHistoryRepository = new OrderHistoryRepository();
        authRepository = AuthRepository.getInstance();

        initViews();
        setupRecyclerView();
        loadOrders();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadOrders);
    }

    private void initViews() {
        rvOrderTracking = findViewById(R.id.rvOrderTracking);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        llEmpty = findViewById(R.id.llEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(new ArrayList<>());
        rvOrderTracking.setLayoutManager(new LinearLayoutManager(this));
        rvOrderTracking.setAdapter(adapter);
    }

    private void loadOrders() {
        String uid = authRepository.getCurrentUser().getUid();
        swipeRefresh.setRefreshing(true);

        orderHistoryRepository.fetchUserOrderHistory(uid, new OrderHistoryRepository.OrderCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);

                List<Order> activeOrders = orders.stream()
                        .filter(order -> !FirebaseConstants.STATUS_COMPLETED.equals(order.getStatus())
                                && !FirebaseConstants.STATUS_CANCELLED.equals(order.getStatus()))
                        .collect(Collectors.toList());

                if (activeOrders.isEmpty()) {
                    llEmpty.setVisibility(View.VISIBLE);
                    rvOrderTracking.setVisibility(View.GONE);
                } else {
                    llEmpty.setVisibility(View.GONE);
                    rvOrderTracking.setVisibility(View.VISIBLE);
                    adapter.updateList(activeOrders);
                }
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderTrackingActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
