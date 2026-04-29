package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milkteamanagement.models.User;
import com.example.milkteamanagement.repositories.AuthRepository;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvName, tvRole, tvEmail, tvPhone, tvAddress;
    private Button btnEditProfile, btnLogout;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ View
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvRole = findViewById(R.id.tvRole);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        authRepository = AuthRepository.getInstance();

        // Tải thông tin người dùng
        loadUserProfile();

        // Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            authRepository.logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserProfile() {
        authRepository.getUserData().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                tvName.setText(user.getFullName());
                tvRole.setText("Vai trò: " + user.getRole());
                tvEmail.setText(user.getEmail());
                tvPhone.setText(user.getPhoneNumber());
                tvAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Chưa cập nhật");
            } else {
                Toast.makeText(this, "Không thể tải thông tin cá nhân", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
