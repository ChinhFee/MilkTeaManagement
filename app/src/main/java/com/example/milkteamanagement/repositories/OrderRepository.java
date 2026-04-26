package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class OrderRepository {
    private final FirebaseFirestore db;

    public interface OrderActionCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface OrderListCallback {
        void onSuccess(List<Order> orders);
        void onFailure(String message);
    }

    public OrderRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Đặt hàng mới
     */
    public void placeOrder(Order order, OrderActionCallback callback) {
        String orderId = db.collection(FirebaseConstants.COL_ORDERS).document().getId();
        order.setOrderId(orderId);
        order.setTimestamp(System.currentTimeMillis());
        order.setStatus(FirebaseConstants.STATUS_PENDING);

        db.collection(FirebaseConstants.COL_ORDERS).document(orderId).set(order)
                .addOnSuccessListener(aVoid -> {
                    CartManager.getInstance().clearCart(); // Xóa giỏ hàng sau khi đặt thành công
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Hủy đơn hàng (Chỉ khi đơn còn PENDING)
     */
    public void cancelOrder(String orderId, OrderActionCallback callback) {
        db.collection(FirebaseConstants.COL_ORDERS).document(orderId)
                .update("status", FirebaseConstants.STATUS_CANCELLED)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * [ADMIN] Lấy tất cả đơn hàng theo trạng thái
     */
    public void getOrdersByStatus(String status, OrderListCallback callback) {
        db.collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("status", status)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = queryDocumentSnapshots.toObjects(Order.class);
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * [ADMIN] Cập nhật trạng thái đơn hàng
     */
    public void updateOrderStatus(String orderId, String newStatus, OrderActionCallback callback) {
        db.collection(FirebaseConstants.COL_ORDERS).document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
