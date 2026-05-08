package com.example.milkteamanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.milkteamanagement.models.Evaluation;
import com.example.milkteamanagement.repositories.EvaluationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageEvaluationsActivity extends AppCompatActivity {

    private RecyclerView rvEvaluations;
    private EvaluationAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvAverageRating, tvTotalEvaluations;
    private RatingBar ratingBarOverall;
    private EvaluationRepository evaluationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_evaluations);

        evaluationRepository = new EvaluationRepository();

        initViews();
        setupRecyclerView();
        loadEvaluations();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        swipeRefresh.setOnRefreshListener(this::loadEvaluations);
    }

    private void initViews() {
        rvEvaluations = findViewById(R.id.rvEvaluations);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalEvaluations = findViewById(R.id.tvTotalEvaluations);
        ratingBarOverall = findViewById(R.id.ratingBarOverall);
    }

    private void setupRecyclerView() {
        adapter = new EvaluationAdapter(new ArrayList<>());
        rvEvaluations.setLayoutManager(new LinearLayoutManager(this));
        rvEvaluations.setAdapter(adapter);
    }

    private void loadEvaluations() {
        swipeRefresh.setRefreshing(true);
        evaluationRepository.getAllEvaluations(new EvaluationRepository.EvaluationListCallback() {
            @Override
            public void onSuccess(List<Evaluation> evaluations) {
                swipeRefresh.setRefreshing(false);
                adapter.updateList(evaluations);
                calculateStats(evaluations);
            }

            @Override
            public void onFailure(String message) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ManageEvaluationsActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateStats(List<Evaluation> evaluations) {
        if (evaluations.isEmpty()) {
            tvAverageRating.setText("0.0");
            ratingBarOverall.setRating(0);
            tvTotalEvaluations.setText("0 đánh giá");
            return;
        }

        float totalRating = 0;
        for (Evaluation eval : evaluations) {
            totalRating += eval.getRating();
        }

        float average = totalRating / evaluations.size();
        tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", average));
        ratingBarOverall.setRating(average);
        tvTotalEvaluations.setText(evaluations.size() + " đánh giá");
    }
}
