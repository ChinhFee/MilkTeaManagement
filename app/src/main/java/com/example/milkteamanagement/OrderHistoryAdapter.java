package com.example.milkteamanagement;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private List<Order> orders;
    private DecimalFormat formatter = new DecimalFormat("#,###đ");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public OrderHistoryAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        
        String orderId = order.getOrderId() == null ? "" : order.getOrderId();
        holder.tvOrderId.setText("Mã ĐH: " + orderId.substring(0, Math.min(orderId.length(), 8)).toUpperCase());
        holder.tvOrderDate.setText(dateFormat.format(new Date(order.getTimestamp())));
        holder.tvOrderTotal.setText(formatter.format(order.getTotalAmount()));
        holder.tvOrderStatus.setText(order.getStatus());

        StringBuilder itemsSummary = new StringBuilder();
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                CartItem item = order.getItems().get(i);
                itemsSummary.append(item.getQuantity()).append("x ").append(item.getProductName());
                if (i < order.getItems().size() - 1) {
                    itemsSummary.append(", ");
                }
            }
        }
        holder.tvOrderItems.setText(itemsSummary.toString());

        int statusColorRes = R.color.primaryColor;
        switch (order.getStatus()) {
            case FirebaseConstants.STATUS_PENDING:
                statusColorRes = R.color.primaryColor;
                break;
            case FirebaseConstants.STATUS_COMPLETED:
                statusColorRes = android.R.color.holo_green_dark;
                break;
            case FirebaseConstants.STATUS_CANCELLED:
                statusColorRes = R.color.errorColor;
                break;
            default:
                statusColorRes = R.color.primaryDarkColor;
                break;
        }
        holder.tvOrderStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), statusColorRes));
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    public void updateList(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderDate, tvOrderStatus, tvOrderId, tvOrderItems, tvOrderTotal;
        Button btnEvaluate;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnEvaluate = itemView.findViewById(R.id.btnEvaluate);
        }
    }
}
