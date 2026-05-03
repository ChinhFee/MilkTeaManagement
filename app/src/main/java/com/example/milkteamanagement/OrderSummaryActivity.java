package com.example.milkteamanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.example.milkteamanagement.repositories.OrderRepository;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderSummaryActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderSummaryAdapter adapter;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        orderRepository = new OrderRepository();
        loadOrders();
    }

    private void loadOrders() {
        orderRepository.getOrders(null, new OrderRepository.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                adapter = new OrderSummaryAdapter(orders);
                rvOrders.setAdapter(adapter);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(OrderSummaryActivity.this, "Access Denied: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder> {
        private final List<Order> orders;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        private final DecimalFormat currencyFormatter = new DecimalFormat("#,###đ");

        public OrderSummaryAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_summary, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(holder.getBindingAdapterPosition());
            String orderId = order.getOrderId() == null ? "" : order.getOrderId();
            holder.tvId.setText("#" + orderId.substring(0, Math.min(orderId.length(), 8)).toUpperCase());
            holder.tvStatus.setText(getStatusText(order.getStatus()));
            holder.tvCustomer.setText(order.getCustomerName());
            holder.tvTime.setText(dateFormat.format(new Date(order.getTimestamp())));
            holder.tvAmount.setText(currencyFormatter.format(order.getTotalAmount()));

            StringBuilder itemsStr = new StringBuilder();
            if (order.getItems() != null) {
                for (CartItem item : order.getItems()) {
                    itemsStr.append(item.getProductName()).append(" (")
                            .append(item.getSugar()).append(", ")
                            .append(item.getIce()).append(") x")
                            .append(item.getQuantity()).append("\n");
                }
            }
            holder.tvItems.setText(itemsStr.toString().trim());

            updateStatusUI(holder, order.getStatus());

            holder.tvId.setOnClickListener(v -> {
                String details = "Khách hàng: " + order.getCustomerName() +
                               "\nSĐT: " + order.getCustomerPhone() +
                               "\nGhi chú: " + (order.getNote() != null ? order.getNote() : "Không có");
                Toast.makeText(OrderSummaryActivity.this, details, Toast.LENGTH_LONG).show();
            });

            holder.tvStatus.setOnClickListener(v -> {
                String nextStatus = getNextStatus(order.getStatus());
                orderRepository.updateOrderStatus(order.getOrderId(), nextStatus, new OrderRepository.OrderActionCallback() {
                    @Override
                    public void onSuccess() {
                        order.setStatus(nextStatus);
                        new FirebaseService(OrderSummaryActivity.this).sendPushNotification(
                                order.getCustomerId(),
                                "Cap nhat don hang",
                                getCustomerStatusMessage(nextStatus)
                        );
                        notifyItemChanged(holder.getBindingAdapterPosition());
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(OrderSummaryActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        private String getStatusText(String status) {
            return status == null ? "" : status;
        }

        private String getNextStatus(String currentStatus) {
            if (FirebaseConstants.STATUS_PENDING.equals(currentStatus)) return FirebaseConstants.STATUS_PROCESSING;
            if (FirebaseConstants.STATUS_PROCESSING.equals(currentStatus)) return FirebaseConstants.STATUS_SHIPPED;
            if (FirebaseConstants.STATUS_SHIPPED.equals(currentStatus)) return FirebaseConstants.STATUS_COMPLETED;
            return FirebaseConstants.STATUS_PENDING;
        }

        private String getCustomerStatusMessage(String status) {
            if (FirebaseConstants.STATUS_PROCESSING.equals(status)) return "Quan dang pha che don hang cua ban.";
            if (FirebaseConstants.STATUS_SHIPPED.equals(status)) return "Don hang cua ban dang duoc giao.";
            if (FirebaseConstants.STATUS_COMPLETED.equals(status)) return "Don hang cua ban da hoan thanh.";
            if (FirebaseConstants.STATUS_CANCELLED.equals(status)) return "Don hang cua ban da bi huy.";
            return "Trang thai don hang da duoc cap nhat: " + status;
        }

        private void updateStatusUI(ViewHolder holder, String status) {
            int color;
            int textColor = Color.WHITE;
            
            if (FirebaseConstants.STATUS_PENDING.equals(status)) {
                color = Color.parseColor("#FFA000");
            } else if (FirebaseConstants.STATUS_PROCESSING.equals(status)) {
                color = Color.parseColor("#1976D2");
            } else if (FirebaseConstants.STATUS_SHIPPED.equals(status)) {
                color = Color.parseColor("#00897B");
            } else if (FirebaseConstants.STATUS_COMPLETED.equals(status)) {
                color = Color.parseColor("#388E3C");
            } else if (FirebaseConstants.STATUS_CANCELLED.equals(status)) {
                color = Color.parseColor("#D32F2F");
            } else {
                color = Color.parseColor("#757575");
            }
            
            holder.statusContainer.setCardBackgroundColor(color);
            holder.tvStatus.setTextColor(textColor);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvId, tvStatus, tvCustomer, tvItems, tvTime, tvAmount;
            CardView statusContainer;

            public ViewHolder(@NonNull View v) {
                super(v);
                tvId = v.findViewById(R.id.tvOrderId);
                tvStatus = v.findViewById(R.id.tvOrderStatus);
                tvCustomer = v.findViewById(R.id.tvCustomerName);
                tvItems = v.findViewById(R.id.tvOrderItems);
                tvTime = v.findViewById(R.id.tvOrderTime);
                tvAmount = v.findViewById(R.id.tvTotalAmount);
                statusContainer = (CardView) tvStatus.getParent();
            }
        }
    }
}
