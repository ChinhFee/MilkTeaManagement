package com.example.milkteamanagement;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.models.Supply;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private BarChart barChart;
    private LineChart lineChart;
    private PieChart pieChart;
    private TextView tvTotalValue, tvAnalyticsTitle, tvDetailTitle;
    private RecyclerView rvDetails;
    private TabLayout tabLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupTabs();
        
        // Mặc định load Tab 1
        loadTask1Comparison();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        tvAnalyticsTitle = findViewById(R.id.tvAnalyticsTitle);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        rvDetails = findViewById(R.id.rvDetails);
        tabLayout = findViewById(R.id.tabLayoutAnalytics);

        rvDetails.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                resetUI();
                switch (tab.getPosition()) {
                    case 0: loadTask1Comparison(); break;
                    case 1: loadDailyAnalytics(); break;
                    case 2: loadWeeklyMonthlyAnalytics(); break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void resetUI() {
        barChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        findViewById(R.id.cardPieChart).setVisibility(View.GONE);
        tvDetailTitle.setVisibility(View.GONE);
        rvDetails.setVisibility(View.GONE);
        barChart.clear();
        lineChart.clear();
        pieChart.clear();
    }

    // TÁC VỤ 1: Thống kê Nhập vs Bán (BarChart)
    private void loadTask1Comparison() {
        tvAnalyticsTitle.setText("So sánh Nhập hàng - Bán ra");
        barChart.setVisibility(View.VISIBLE);

        db.collection(FirebaseConstants.COL_SUPPLY).get().addOnSuccessListener(supplyDocs -> {
            double totalInput = 0;
            Map<String, Supply.SupplyItem> inputDetails = new HashMap<>();
            for (QueryDocumentSnapshot doc : supplyDocs) {
                Supply supply = doc.toObject(Supply.class);
                totalInput += supply.getTotalCost();
                if (supply.getItems() != null) {
                    for (Supply.SupplyItem item : supply.getItems()) {
                        Supply.SupplyItem existing = inputDetails.get(item.getItemName());
                        if (existing == null) {
                            inputDetails.put(item.getItemName(), item);
                        } else {
                            existing.setQuantity(existing.getQuantity() + item.getQuantity());
                        }
                    }
                }
            }

            double finalTotalInput = totalInput;
            db.collection(FirebaseConstants.COL_ORDERS)
                    .whereEqualTo("status", FirebaseConstants.STATUS_COMPLETED)
                    .get().addOnSuccessListener(orderDocs -> {
                double totalSales = 0;
                Map<String, CartItem> salesDetails = new HashMap<>();
                for (QueryDocumentSnapshot doc : orderDocs) {
                    Order order = doc.toObject(Order.class);
                    totalSales += order.getTotalAmount();
                    for (CartItem item : order.getItems()) {
                        CartItem existing = salesDetails.get(item.getProductName());
                        if (existing == null) {
                            salesDetails.put(item.getProductName(), item);
                        } else {
                            existing.setQuantity(existing.getQuantity() + item.getQuantity());
                        }
                    }
                }

                showBarChartTask1(finalTotalInput, totalSales, inputDetails, salesDetails);
            });
        });
    }

    private void showBarChartTask1(double input, double sales, Map<String, Supply.SupplyItem> inDetails, Map<String, CartItem> outDetails) {
        tvTotalValue.setText(String.format(Locale.getDefault(), "Lợi nhuận: %,.0fđ", (sales - input)));
        
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) input));
        entries.add(new BarEntry(1, (float) sales));

        BarDataSet dataSet = new BarDataSet(entries, "Giá trị (VND)");
        dataSet.setColors(new int[]{Color.RED, Color.GREEN});
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Hàng nhập", "Bán được"}));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.animateY(1000);
        barChart.invalidate();

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                tvDetailTitle.setVisibility(View.VISIBLE);
                rvDetails.setVisibility(View.VISIBLE);
                if (e.getX() == 0) {
                    tvDetailTitle.setText("Chi tiết hàng nhập (Tổng: " + String.format("%,.0fđ", input) + ")");
                    // TODO: Gán Adapter cho rvDetails với inDetails
                } else {
                    tvDetailTitle.setText("Chi tiết sản phẩm bán (Tổng: " + String.format("%,.0fđ", sales) + ")");
                    // TODO: Gán Adapter cho rvDetails với outDetails
                }
            }
            @Override
            public void onNothingSelected() {}
        });
    }

    // TÁC VỤ 2: Doanh thu ngày (LineChart + PieChart)
    private void loadDailyAnalytics() {
        tvAnalyticsTitle.setText("Doanh thu hôm nay");
        lineChart.setVisibility(View.VISIBLE);
        findViewById(R.id.cardPieChart).setVisibility(View.VISIBLE);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long startOfDay = cal.getTimeInMillis();

        db.collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("status", FirebaseConstants.STATUS_COMPLETED)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            
            float[] hourlyRevenue = new float[24];
            Map<String, Integer> productCounts = new HashMap<>();
            double totalDay = 0;

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                totalDay += order.getTotalAmount();
                
                Calendar orderCal = Calendar.getInstance();
                orderCal.setTimeInMillis(order.getTimestamp());
                int hour = orderCal.get(Calendar.HOUR_OF_DAY);
                hourlyRevenue[hour] += order.getTotalAmount();

                for (CartItem item : order.getItems()) {
                    productCounts.put(item.getProductName(), productCounts.getOrDefault(item.getProductName(), 0) + item.getQuantity());
                }
            }

            tvTotalValue.setText(String.format(Locale.getDefault(), "%,.0fđ", totalDay));
            showLineChart(hourlyRevenue, "Giờ trong ngày");
            showPieChart(productCounts);
        });
    }

    private void showLineChart(float[] dataPoints, String label) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataPoints.length; i++) {
            entries.add(new Entry(i, dataPoints[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.CYAN);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void showPieChart(Map<String, Integer> counts) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.MAGENTA, Color.YELLOW, Color.CYAN, Color.GREEN, Color.LTGRAY});
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setCenterText("Bán chạy");
        pieChart.animateXY(1000, 1000);
        pieChart.invalidate();
    }

    // TÁC VỤ 3: Tuần/Tháng (Tương tự nhưng lọc thời gian)
    private void loadWeeklyMonthlyAnalytics() {
        // Có thể thêm Dialog chọn Tuần hoặc Tháng
        tvAnalyticsTitle.setText("Doanh thu 7 ngày qua");
        lineChart.setVisibility(View.VISIBLE);
        findViewById(R.id.cardPieChart).setVisibility(View.VISIBLE);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long startOfPeriod = cal.getTimeInMillis();

        db.collection(FirebaseConstants.COL_ORDERS)
                .whereEqualTo("status", FirebaseConstants.STATUS_COMPLETED)
                .whereGreaterThanOrEqualTo("timestamp", startOfPeriod)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            
            Map<Integer, Float> dailyRevenue = new HashMap<>();
            Map<String, Integer> productCounts = new HashMap<>();
            double totalPeriod = 0;

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                totalPeriod += order.getTotalAmount();
                
                Calendar orderCal = Calendar.getInstance();
                orderCal.setTimeInMillis(order.getTimestamp());
                int day = orderCal.get(Calendar.DAY_OF_MONTH);
                dailyRevenue.put(day, dailyRevenue.getOrDefault(day, 0f) + (float)order.getTotalAmount());

                for (CartItem item : order.getItems()) {
                    productCounts.put(item.getProductName(), productCounts.getOrDefault(item.getProductName(), 0) + item.getQuantity());
                }
            }

            tvTotalValue.setText(String.format(Locale.getDefault(), "%,.0fđ", totalPeriod));
            
            // Vẽ biểu đồ cho 7 ngày qua
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, -i);
                int day = c.get(Calendar.DAY_OF_MONTH);
                entries.add(new Entry(6-i, dailyRevenue.getOrDefault(day, 0f)));
            }
            // Vẽ LineChart (tương tự showLineChart)
            showPieChart(productCounts);
        });
    }
}
