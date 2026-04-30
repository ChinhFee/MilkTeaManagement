package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ManageOrdersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        CardView cardOrderSummary = findViewById(R.id.cardOrderSummary);
        CardView cardTableMap = findViewById(R.id.cardTableMap);

        cardOrderSummary.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderSummaryActivity.class));
        });

        cardTableMap.setOnClickListener(v -> {
            startActivity(new Intent(this, TableMapActivity.class));
        });
    }
}
