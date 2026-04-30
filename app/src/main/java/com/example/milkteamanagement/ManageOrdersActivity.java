package com.example.milkteamanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.example.milkteamanagement.repositories.OrderRepository;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageOrdersActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private OrderRepository orderRepository;
    private List<Order> orderList = new ArrayList<>();
    private String currentStatus = FirebaseConstants.STATUS_PENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        initViews();
        orderRepository = new OrderRepository();
        
        loadOrders(currentStatus);
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        tabLayout = findViewById(R.id.tabLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvOrders = findViewById(R.id.rvOrders);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList);
        rvOrders.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatus = FirebaseConstants.STATUS_PENDING;
                        break;
                    case 1:
                        currentStatus = FirebaseConstants.STATUS_PROCESSING;
                        break;
                    case 2:
                        currentStatus = FirebaseConstants.STATUS_COMPLETED;
                        break;
                }
                loadOrders(currentStatus);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh.setOnRefreshListener(() -> loadOrders(currentStatus));
    }

    private void loadOrders(String status) {
        swipeRefresh.setRefreshing(true);
        orderRepository.getOrdersByStatus(status, new OrderRepository.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                orderList.clear();
                orderList.addAll(orders);
                adapter.notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ManageOrdersActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private final List<Order> orders;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public OrderAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.tvId.setText("#" + order.getOrderId().substring(0, 8).toUpperCase());
            holder.tvDate.setText(dateFormat.format(new Date(order.getTimestamp())));
            holder.tvTotal.setText(String.format(Locale.getDefault(), "%,.0fđ", order.getTotalAmount()));
            
            StringBuilder items = new StringBuilder();
            for (CartItem item : order.getItems()) {
                items.append(item.getProductName()).append(" x").append(item.getQuantity()).append(", ");
            }
            String itemsStr = items.toString();
            if (itemsStr.length() > 2) itemsStr = itemsStr.substring(0, itemsStr.length() - 2);
            holder.tvItems.setText(itemsStr);

            holder.tvStatus.setText(getStatusVN(order.getStatus()));
            updateStatusUI(holder.tvStatus, order.getStatus());

            holder.itemView.setOnClickListener(v -> {
                // Hiển thị dialog xác nhận đổi trạng thái
                showStatusUpdateDialog(order);
            });
        }

        private String getStatusVN(String status) {
            switch (status) {
                case FirebaseConstants.STATUS_PENDING: return "Chờ duyệt";
                case FirebaseConstants.STATUS_PROCESSING: return "Đang làm";
                case FirebaseConstants.STATUS_COMPLETED: return "Hoàn thành";
                case FirebaseConstants.STATUS_CANCELLED: return "Đã hủy";
                default: return status;
            }
        }

        private void updateStatusUI(TextView tvStatus, String status) {
            int bgRes = R.drawable.bg_status_pending;
            switch (status) {
                case FirebaseConstants.STATUS_PROCESSING:
                    bgRes = R.drawable.bg_status_pending; // Có thể thay bằng màu khác
                    break;
                case FirebaseConstants.STATUS_COMPLETED:
                    bgRes = R.drawable.bg_status_pending; // Có thể thay bằng màu khác
                    break;
            }
            tvStatus.setBackgroundResource(bgRes);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvStatus, tvId, tvItems, tvTotal;

            public ViewHolder(@NonNull View v) {
                super(v);
                tvDate = v.findViewById(R.id.tvOrderDate);
                tvStatus = v.findViewById(R.id.tvOrderStatus);
                tvId = v.findViewById(R.id.tvOrderId);
                tvItems = v.findViewById(R.id.tvOrderItems);
                tvTotal = v.findViewById(R.id.tvOrderTotal);
            }
        }
    }

    private void showStatusUpdateDialog(Order order) {
        String nextStatus = "";
        String actionLabel = "";

        if (order.getStatus().equals(FirebaseConstants.STATUS_PENDING)) {
            nextStatus = FirebaseConstants.STATUS_PROCESSING;
            actionLabel = "Xác nhận làm đơn";
        } else if (order.getStatus().equals(FirebaseConstants.STATUS_PROCESSING)) {
            nextStatus = FirebaseConstants.STATUS_COMPLETED;
            actionLabel = "Hoàn thành đơn";
        }

        if (nextStatus.isEmpty()) return;

        String finalNextStatus = nextStatus;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cập nhật trạng thái")
                .setMessage("Bạn muốn chuyển đơn hàng này sang '" + actionLabel + "'?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    updateStatus(order.getOrderId(), finalNextStatus);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateStatus(String orderId, String newStatus) {
        orderRepository.updateOrderStatus(orderId, newStatus, new OrderRepository.OrderActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ManageOrdersActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                loadOrders(currentStatus);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ManageOrdersActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
