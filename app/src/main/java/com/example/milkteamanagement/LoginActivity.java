package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.milkteamanagement.models.User;
import com.example.milkteamanagement.repositories.AuthRepository;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        authRepository = AuthRepository.getInstance();

        if (authRepository.isLoggedIn()) {
            checkUserRoleAndNavigate();
        }

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        authRepository.login(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    checkUserRoleAndNavigate();
                } else {
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void checkUserRoleAndNavigate() {
        authRepository.getUserData().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                updateFCMToken(user.getUid());
                if ("admin".equalsIgnoreCase(user.getRole())) {
                    FirebaseMessaging.getInstance().subscribeToTopic("admins");
                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("admins");
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
                finish();
            } else {
                btnLogin.setEnabled(true);
                authRepository.logout();
                Toast.makeText(this, "Lỗi khi kiểm tra quyền truy cập", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFCMToken(String userId) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                Map<String, Object> updates = new HashMap<>();
                updates.put("fcmToken", token);

                FirebaseFirestore.getInstance().collection(FirebaseConstants.COL_USERS)
                        .document(userId)
                        .update(updates);
            }
        });
    }
}
