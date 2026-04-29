package com.example.milkteamanagement;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FirebaseService {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;

    public FirebaseService(Context context) {
        this.context = context;
    }

    public interface OnProductsLoadedListener {
        void onSuccess(List<Product> products);
        void onFailure(Exception e);
    }

    public void getAllProducts(OnProductsLoadedListener listener) {
        db.collection(FirebaseConstants.COL_PRODUCTS)
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> list = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            list.add(doc.toObject(Product.class));
                        }
                        listener.onSuccess(list);
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public interface OnOrderProcessListener {
        void onSuccess(String orderId);
        void onFailure(String message);
    }

    public void placeOrder(Order order, OnOrderProcessListener listener) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            listener.onFailure("Giỏ hàng trống!");
            return;
        }
        db.collection(FirebaseConstants.COL_ORDERS)
                .document(order.getOrderId())
                .set(order)
                .addOnSuccessListener(aVoid -> listener.onSuccess(order.getOrderId()))
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void startListeningOrder(String orderId) {
        db.collection(FirebaseConstants.COL_ORDERS).document(orderId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    String status = snapshot.getString("status");
                    handleStatusNotification(status);
                });
    }

    private void handleStatusNotification(String status) {
        if (status == null) return;
        String message = "";
        switch (status) {
            case FirebaseConstants.STATUS_PROCESSING:
                message = "Quán đang làm trà sữa cho bạn!";
                break;
            case FirebaseConstants.STATUS_SHIPPED:
                message = "Tài xế đang giao hàng!";
                break;
            case FirebaseConstants.STATUS_COMPLETED:
                message = "Đơn hàng đã hoàn thành. Enjoy!";
                break;
            case FirebaseConstants.STATUS_CANCELLED:
                message = "Rất tiếc, đơn hàng đã bị hủy.";
                break;
            default:
                return;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        vibratePhone();
    }

    private void vibratePhone() {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }
        }
    }
}
