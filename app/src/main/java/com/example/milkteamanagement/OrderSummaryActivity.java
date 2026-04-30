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
            holder.tvId.setText("#" + order.getOrderId().substring(0, 8).toUpperCase());
            holder.tvStatus.setText(getStatusText(order.getStatus()));
            holder.tvCustomer.setText(order.getCustomerName());
            holder.tvTime.setText(dateFormat.format(new Date(order.getTimestamp())));
            holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalAmount()));

            StringBuilder itemsStr = new StringBuilder();
            if (order.getItems() != null) {
                for (CartItem item : order.getItems()) {
                    itemsStr.append(item.getProductName()).append(" x").append(item.getQuantity()).append("\n");
                }
            }
            holder.tvItems.setText(itemsStr.toString().trim());

            updateStatusUI(holder, order.getStatus());

            holder.tvId.setOnClickListener(v -> {
                String details = "Customer: " + order.getCustomerName() + 
                               "\nPhone: " + order.getCustomerPhone() + 
                               "\nNote: " + (order.getNote() != null ? order.getNote() : "N/A");
                Toast.makeText(OrderSummaryActivity.this, details, Toast.LENGTH_LONG).show();
            });

            holder.tvStatus.setOnClickListener(v -> {
                String nextStatus = getNextStatus(order.getStatus());
                orderRepository.updateOrderStatus(order.getOrderId(), nextStatus, new OrderRepository.OrderActionCallback() {
                    @Override
                    public void onSuccess() {
                        order.setStatus(nextStatus);
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
            if (status.equals(FirebaseConstants.STATUS_PENDING)) return "PENDING";
            if (status.equals(FirebaseConstants.STATUS_PROCESSING)) return "PROCESSING";
            if (status.equals(FirebaseConstants.STATUS_COMPLETED)) return "COMPLETED";
            return status.toUpperCase();
        }

        private String getNextStatus(String currentStatus) {
            if (currentStatus.equals(FirebaseConstants.STATUS_PENDING)) return FirebaseConstants.STATUS_PROCESSING;
            if (currentStatus.equals(FirebaseConstants.STATUS_PROCESSING)) return FirebaseConstants.STATUS_COMPLETED;
            return FirebaseConstants.STATUS_PENDING;
        }

        private void updateStatusUI(ViewHolder holder, String status) {
            int color;
            int textColor = Color.WHITE;
            
            if (status.equals(FirebaseConstants.STATUS_PENDING)) {
                color = ContextCompat.getColor(OrderSummaryActivity.this, R.color.successColor);
            } else if (status.equals(FirebaseConstants.STATUS_PROCESSING)) {
                color = ContextCompat.getColor(OrderSummaryActivity.this, R.color.errorColor);
            } else if (status.equals(FirebaseConstants.STATUS_COMPLETED)) {
                color = Color.parseColor("#C89D32"); // AuraBOBA Gold
            } else {
                color = Color.TRANSPARENT;
                textColor = Color.BLACK;
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
