package com.example.milkteamanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {
    private LineChart lineChart;
    private PieChart pieChart;
    private TextView tvTotalValue, tvAnalyticsTitle;
    private TabLayout tabLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupTabs();
        loadDailyAnalytics();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        tvAnalyticsTitle = findViewById(R.id.tvAnalyticsTitle);
        tabLayout = findViewById(R.id.tabLayoutAnalytics);
    }

    private long days(int value) {
        return value * 24L * 60L * 60L * 1000L;
    }

    private long hours(int value) {
        return value * 60L * 60L * 1000L;
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                resetCharts();
                if (tab.getPosition() == 0) {
                    loadDailyAnalytics();
                } else {
                    loadWeeklyMonthlyAnalytics();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void resetCharts() {
        findViewById(R.id.cardPieChart).setVisibility(View.GONE);
        lineChart.clear();
        pieChart.clear();
    }

    private void loadDailyAnalytics() {
        tvAnalyticsTitle.setText("Doanh thu hôm nay");
        findViewById(R.id.cardPieChart).setVisibility(View.VISIBLE);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        db.collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("status", FirebaseConstants.STATUS_COMPLETED)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    float[] hourlyRevenue = new float[24];
                    Map<String, Integer> productCounts = new HashMap<>();
                    double totalDay = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order.getTimestamp() < startOfDay) {
                            continue;
                        }

                        totalDay += order.getTotalAmount();

                        Calendar orderCal = Calendar.getInstance();
                        orderCal.setTimeInMillis(order.getTimestamp());
                        int hour = orderCal.get(Calendar.HOUR_OF_DAY);
                        hourlyRevenue[hour] += order.getTotalAmount();

                        addProductCounts(productCounts, order);
                    }

                    tvTotalValue.setText(String.format(Locale.getDefault(), "%,.0fđ", totalDay));
                    showLineChart(hourlyRevenue, buildHourLabels(), "Doanh thu theo giờ");
                    showPieChart(productCounts);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadWeeklyMonthlyAnalytics() {
        tvAnalyticsTitle.setText("Doanh thu 7 ngày qua");
        findViewById(R.id.cardPieChart).setVisibility(View.VISIBLE);

        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, -6);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        db.collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("status", FirebaseConstants.STATUS_COMPLETED)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    float[] dailyRevenue = new float[7];
                    List<String> labels = buildSevenDayLabels();
                    Map<String, Integer> productCounts = new HashMap<>();
                    double totalPeriod = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        int index = daysBetween(start.getTimeInMillis(), order.getTimestamp());
                        if (index < 0 || index >= dailyRevenue.length) {
                            continue;
                        }

                        totalPeriod += order.getTotalAmount();
                        dailyRevenue[index] += order.getTotalAmount();
                        addProductCounts(productCounts, order);
                    }

                    tvTotalValue.setText(String.format(Locale.getDefault(), "%,.0fđ", totalPeriod));
                    showLineChart(dailyRevenue, labels, "Doanh thu theo ngày");
                    showPieChart(productCounts);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addProductCounts(Map<String, Integer> productCounts, Order order) {
        if (order.getItems() == null) return;

        for (CartItem item : order.getItems()) {
            String productName = item.getProductName() == null ? "Khác" : item.getProductName();
            productCounts.put(productName, productCounts.getOrDefault(productName, 0) + item.getQuantity());
        }
    }

    private void showLineChart(float[] dataPoints, List<String> labels, String label) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataPoints.length; i++) {
            entries.add(new Entry(i, dataPoints[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.rgb(200, 157, 50));
        dataSet.setCircleColor(Color.rgb(142, 110, 29));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.rgb(229, 195, 110));

        lineChart.setData(new LineData(dataSet));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.animateX(700);
        lineChart.invalidate();
    }

    private void showPieChart(Map<String, Integer> counts) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.rgb(200, 157, 50),
                Color.rgb(102, 187, 106),
                Color.rgb(63, 81, 181),
                Color.rgb(239, 83, 80),
                Color.rgb(78, 52, 46)
        });
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        pieChart.setData(new PieData(dataSet));
        pieChart.setCenterText("Bán chạy");
        pieChart.animateXY(700, 700);
        pieChart.invalidate();
    }

    private List<String> buildHourLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    private List<String> buildSevenDayLabels() {
        List<String> labels = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            labels.add(formatter.format(day.getTime()));
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
        return labels;
    }

    private int daysBetween(long startTimestamp, long orderTimestamp) {
        long diff = orderTimestamp - startTimestamp;
        return (int) (diff / (24L * 60L * 60L * 1000L));
    }
}
