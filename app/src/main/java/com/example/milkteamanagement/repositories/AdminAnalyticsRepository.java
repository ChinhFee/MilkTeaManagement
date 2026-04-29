package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.models.Topping;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AdminAnalyticsRepository {
    private final FirebaseFirestore db;

    public interface AnalyticsCallback {
        void onRevenueResult(double totalRevenue, int orderCount);
        void onBestSellerResult(String productName, int quantity);
        void onError(String message);
    }

    public AdminAnalyticsRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void getRevenueStats(long startTime, long endTime, AnalyticsCallback callback) {
        db.collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("status", FirebaseConstants.STATUS_COMPLETED)
                .whereGreaterThanOrEqualTo("timestamp", startTime)
                .whereLessThanOrEqualTo("timestamp", endTime)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRevenue = 0;
                    int count = queryDocumentSnapshots.size();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        totalRevenue += order.getTotalAmount();
                    }
                    callback.onRevenueResult(totalRevenue, count);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getBestSellers(AnalyticsCallback callback) {
        db.collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("status", FirebaseConstants.STATUS_COMPLETED)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> productCount = new HashMap<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        if (order.getItems() != null) {
                            for (CartItem item : order.getItems()) {
                                String name = item.getProductName();
                                productCount.put(name, productCount.getOrDefault(name, 0) + item.getQuantity());
                            }
                        }
                    }

                    String bestProduct = "N/A";
                    int maxQty = 0;
                    for (Map.Entry<String, Integer> entry : productCount.entrySet()) {
                        if (entry.getValue() > maxQty) {
                            maxQty = entry.getValue();
                            bestProduct = entry.getKey();
                        }
                    }
                    callback.onBestSellerResult(bestProduct, maxQty);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
