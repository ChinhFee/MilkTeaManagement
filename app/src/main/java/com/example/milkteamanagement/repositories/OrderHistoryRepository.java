package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class OrderHistoryRepository {
    private final FirebaseFirestore db;

    public interface OrderCallback {
        void onSuccess(List<Order> orders);
        void onError(String message);
    }

    public OrderHistoryRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void fetchUserOrderHistory(String customerId, OrderCallback callback) {
        if (customerId == null || customerId.isEmpty()) {
            callback.onError("ID khách hàng không hợp lệ");
            return;
        }

        db.collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("customerId", customerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = queryDocumentSnapshots.toObjects(Order.class);
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
