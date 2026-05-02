package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.Order;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class OrderRepository {
    private final FirebaseFirestore db;
    private final MaterialRepository materialRepository;

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
        materialRepository = new MaterialRepository();
    }

    public void placeOrder(Order order, OrderActionCallback callback) {
        String orderId = db.collection(FirebaseConstants.COL_ORDERS).document().getId();
        order.setOrderId(orderId);
        order.setTimestamp(System.currentTimeMillis());
        order.setStatus(FirebaseConstants.STATUS_PENDING);
        order.setMaterialsDeducted(false);

        db.collection(FirebaseConstants.COL_ORDERS).document(orderId).set(order)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void cancelOrder(String orderId, OrderActionCallback callback) {
        db.collection(FirebaseConstants.COL_ORDERS).document(orderId)
                .update("status", FirebaseConstants.STATUS_CANCELLED)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getOrders(String status, OrderListCallback callback) {
        Query query = db.collection(FirebaseConstants.COL_ORDERS);
        if (status != null) {
            query = query.whereEqualTo("status", status);
        }
        query.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = queryDocumentSnapshots.toObjects(Order.class);
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateOrderStatus(String orderId, String newStatus, OrderActionCallback callback) {
        // Lấy thông tin đơn hàng hiện tại để kiểm tra việc trừ kho
        db.collection(FirebaseConstants.COL_ORDERS).document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Order order = documentSnapshot.toObject(Order.class);
                    if (order == null) {
                        callback.onFailure("Không tìm thấy đơn hàng");
                        return;
                    }

                    // Nếu chuyển sang trạng thái pha chế và chưa trừ kho
                    if (newStatus.equals(FirebaseConstants.STATUS_PROCESSING) && !order.isMaterialsDeducted()) {
                        materialRepository.deductMaterialsFromOrder(order)
                                .addOnSuccessListener(aVoid -> {
                                    // Sau khi trừ kho thành công, cập nhật trạng thái đơn hàng và cờ hiệu
                                    db.collection(FirebaseConstants.COL_ORDERS).document(orderId)
                                            .update("status", newStatus, "materialsDeducted", true)
                                            .addOnSuccessListener(v -> callback.onSuccess())
                                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                                })
                                .addOnFailureListener(e -> callback.onFailure("Lỗi trừ kho: " + e.getMessage()));
                    } else {
                        // Cập nhật trạng thái bình thường
                        db.collection(FirebaseConstants.COL_ORDERS).document(orderId)
                                .update("status", newStatus)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
