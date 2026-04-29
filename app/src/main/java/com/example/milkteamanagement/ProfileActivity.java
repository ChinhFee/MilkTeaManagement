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
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, 100);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadUserProfile();
        }
    }

    private void loadUserProfile() {
        authRepository.getUserData().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                tvName.setText(user.getFullName());
                tvRole.setText("Vai trò: " + user.getRole());
                tvEmail.setText("Email: " + user.getEmail());
                tvPhone.setText("SĐT: " + user.getPhoneNumber());
                tvAddress.setText("Địa chỉ: " + (user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Chưa cập nhật"));

                // Thiết lập hình đại diện mặc định dựa trên giới tính
                if ("Nữ".equalsIgnoreCase(user.getGender())) {
                    imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery); // Bạn có thể thay bằng icon nữ của bạn (ví dụ: R.drawable.ic_female)
                } else {
                    imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery); // Bạn có thể thay bằng icon nam của bạn (ví dụ: R.drawable.ic_male)
                }
            } else {
                Toast.makeText(this, "Không thể tải thông tin cá nhân", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
