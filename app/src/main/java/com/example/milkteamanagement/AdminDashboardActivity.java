package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.milkteamanagement.repositories.AuthRepository;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvAdminGreeting;

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

        cardManageMenu.setOnClickListener(v -> startActivity(new Intent(this, ManageMenuActivity.class)));
        cardManageOrders.setOnClickListener(v -> startActivity(new Intent(this, ManageOrdersActivity.class)));
        cardAnalytics.setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));

        cardLogout.setOnClickListener(v -> {
            AuthRepository.getInstance().logout();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
}
