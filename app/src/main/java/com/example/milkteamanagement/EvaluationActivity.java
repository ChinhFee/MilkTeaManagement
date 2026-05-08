package com.example.milkteamanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milkteamanagement.models.Evaluation;
import com.example.milkteamanagement.repositories.AuthRepository;
import com.example.milkteamanagement.repositories.EvaluationRepository;

public class EvaluationActivity extends AppCompatActivity {

    private String orderId;
    private EvaluationRepository evaluationRepository;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        evaluationRepository = new EvaluationRepository();
        authRepository = AuthRepository.getInstance();

        TextView tvOrderId = findViewById(R.id.tvOrderId);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText etComment = findViewById(R.id.etComment);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        tvOrderId.setText("Mã đơn hàng: #" + orderId);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            submitEvaluation(rating, comment);
        });
    }

    private void submitEvaluation(float rating, String comment) {
        String uid = authRepository.getCurrentUser().getUid();
        String name = authRepository.getCurrentUser().getDisplayName();
        if (name == null || name.isEmpty()) name = "Khách hàng";

        Evaluation evaluation = new Evaluation(
                null,
                orderId,
                uid,
                name,
                rating,
                comment,
                System.currentTimeMillis()
        );

        evaluationRepository.addEvaluation(evaluation, new EvaluationRepository.EvaluationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EvaluationActivity.this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(EvaluationActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
