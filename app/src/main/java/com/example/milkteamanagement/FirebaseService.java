package com.example.milkteamanagement;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.widget.Toast;

import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirebaseService {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private static final String PROJECT_ID = "milkteamanagement-6f51f";
    private static final String FCM_V1_URL = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";

    public FirebaseService(Context context) {
        this.context = context;
    }

    public void subscribeToAdmins() {
        FirebaseMessaging.getInstance().subscribeToTopic("admins");
    }

    public void sendPushNotification(String customerId, String title, String body) {
        db.collection(FirebaseConstants.COL_USERS).document(customerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String token = documentSnapshot.getString("fcmToken");
                    if (token != null && !token.isEmpty()) {
                        getAccessTokenAndSend(token, title, body, false);
                    }
                });
    }

    public void sendNotificationToTopic(String topic, String title, String body) {
        getAccessTokenAndSend(topic, title, body, true);
    }

    private void getAccessTokenAndSend(String target, String title, String body, boolean isTopic) {
        new Thread(() -> {
            try {
                // Đọc chuỗi Base64 từ BuildConfig (đã cấu hình trong build.gradle.kts và local.properties)
                String base64Key = BuildConfig.FIREBASE_SERVICE_ACCOUNT_BASE64;
                if (base64Key == null || base64Key.isEmpty()) {
                    return;
                }

                // Giải mã Base64 thành InputStream để GoogleCredentials có thể đọc
                byte[] decodedKey = Base64.decode(base64Key, Base64.DEFAULT);
                InputStream is = new ByteArrayInputStream(decodedKey);

                GoogleCredentials credentials = GoogleCredentials.fromStream(is)
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
                credentials.refreshIfExpired();
                String accessToken = credentials.getAccessToken().getTokenValue();

                sendToFCMV1(target, title, body, accessToken, isTopic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendToFCMV1(String target, String title, String body, String accessToken, boolean isTopic) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        try {
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);

            JSONObject data = new JSONObject();
            data.put("title", title);
            data.put("body", body);

            JSONObject androidNotification = new JSONObject();
            androidNotification.put("channel_id", NotificationHelper.CHANNEL_ID);

            JSONObject android = new JSONObject();
            android.put("priority", "HIGH");
            android.put("notification", androidNotification);

            JSONObject message = new JSONObject();
            if (isTopic) {
                message.put("topic", target);
            } else {
                message.put("token", target);
            }
            message.put("notification", notification);
            message.put("data", data);
            message.put("android", android);

            JSONObject jsonMain = new JSONObject();
            jsonMain.put("message", message);

            RequestBody requestBody = RequestBody.create(jsonMain.toString(), mediaType);
            Request request = new Request.Builder()
                    .url(FCM_V1_URL)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        System.out.println("Notification sent successfully");
                    } else {
                        System.err.println("FCM Error: " + response.body().string());
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Các phương thức hỗ trợ khác ---

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
