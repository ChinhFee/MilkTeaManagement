package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.repositories.AuthRepository;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvAdminGreeting;
    private ListenerRegistration newOrderListener;
    private long listenerStartedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);
        CardView cardManageMenu = findViewById(R.id.cardManageMenu);
        CardView cardManageOrders = findViewById(R.id.cardManageOrders);
        CardView cardAnalytics = findViewById(R.id.cardAnalytics);
        CardView cardLogout = findViewById(R.id.cardLogout);

        // Fetch and display Admin name
        loadAdminProfile();
        NotificationHelper.requestPostNotificationsIfNeeded(this);
        FirebaseMessaging.getInstance().subscribeToTopic("admins");
        startNewOrderNotifications();

        cardManageMenu.setOnClickListener(v -> startActivity(new Intent(this, ManageMenuActivity.class)));
        cardManageOrders.setOnClickListener(v -> startActivity(new Intent(this, ManageOrdersActivity.class)));
        cardAnalytics.setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));

        cardLogout.setOnClickListener(v -> {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("admins").addOnCompleteListener(task -> {
                AuthRepository.getInstance().logout();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    private void loadAdminProfile() {
        AuthRepository.getInstance().getUserData().addOnSuccessListener(user -> {
            if (user != null && user.getFullName() != null) {
                tvAdminGreeting.setText("Hello, " + user.getFullName());
            } else {
                tvAdminGreeting.setText("Hello, Admin");
            }
        }).addOnFailureListener(e -> tvAdminGreeting.setText("Hello, Admin"));
    }

    private void startNewOrderNotifications() {
        listenerStartedAt = System.currentTimeMillis();
        newOrderListener = FirebaseFirestore.getInstance()
                .collection(FirebaseConstants.COL_ORDERS)
                .whereGreaterThanOrEqualTo("timestamp", listenerStartedAt)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        return;
                    }

                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() != DocumentChange.Type.ADDED) {
                            continue;
                        }

                        Order order = change.getDocument().toObject(Order.class);
                        if (order.getTimestamp() < listenerStartedAt) {
                            continue;
                        }

                        String customerName = order.getCustomerName() != null
                                ? order.getCustomerName()
                                : "Khach hang";
                        LocalNotificationSender.show(
                                this,
                                "Don hang moi",
                                customerName + " vua dat don hang moi."
                        );
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (newOrderListener != null) {
            newOrderListener.remove();
            newOrderListener = null;
        }
        super.onDestroy();
    }
}
