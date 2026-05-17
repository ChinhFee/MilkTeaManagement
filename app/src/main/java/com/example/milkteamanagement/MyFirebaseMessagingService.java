package com.example.milkteamanagement;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessaging";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if (title == null) {
            title = remoteMessage.getData().get("title");
        }
        if (body == null) {
            body = remoteMessage.getData().get("body");
        }

        if (title != null || body != null) {
            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + body);
            sendNotification(
                    title != null ? title : getString(R.string.app_name),
                    body != null ? body : ""
            );
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("fcmToken", token);
            FirebaseFirestore.getInstance().collection(FirebaseConstants.COL_USERS).document(userId)
                    .update(tokenMap)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating token", e));
        }
    }

    private void sendNotification(String title, String messageBody) {
        LocalNotificationSender.show(this, title, messageBody);
    }
}
