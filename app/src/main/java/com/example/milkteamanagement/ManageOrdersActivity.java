package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ManageOrdersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.cardOrderSummary)
                .setOnClickListener(v -> startActivity(new Intent(this, OrderSummaryActivity.class)));
        findViewById(R.id.cardTableMap)
                .setOnClickListener(v -> startActivity(new Intent(this, TableMapActivity.class)));
    }
}
