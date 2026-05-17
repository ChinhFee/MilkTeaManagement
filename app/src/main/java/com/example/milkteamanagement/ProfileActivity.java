package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milkteamanagement.models.User;
import com.example.milkteamanagement.repositories.AuthRepository;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.net.Uri;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvName, tvEmail, tvPhone, tvAddress;
    private AuthRepository authRepository;

    private final ActivityResultLauncher<String> pickAvatarLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImage(uri, "avatars");
                }
            }
    );

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadUserProfile();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnChangeAvatar).setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        authRepository = AuthRepository.getInstance();

        loadUserProfile();

        btnLogout.setOnClickListener(v -> {
            authRepository.logout();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        findViewById(R.id.btnOrderHistory).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, OrderHistoryActivity.class));
        });

        findViewById(R.id.btnOrderTracking).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, OrderTrackingActivity.class));
        });
    }

    private void loadUserProfile() {
        authRepository.getUserData().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                tvName.setText(user.getFullName());
                tvEmail.setText(getString(R.string.profile_email, user.getEmail()));
                tvPhone.setText(getString(R.string.profile_phone, user.getPhoneNumber()));
                String address = (user.getAddress() != null && !user.getAddress().isEmpty()) 
                        ? user.getAddress() 
                        : getString(R.string.profile_address_not_updated);
                tvAddress.setText(getString(R.string.profile_address, address));

                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(this)
                            .load(user.getAvatarUrl())
                            .circleCrop()
                            .into(imgAvatar);
                } else {
                    if ("Nữ".equalsIgnoreCase(user.getGender())) {
                        imgAvatar.setImageResource(R.drawable.ic_gender_female);
                    } else {
                        imgAvatar.setImageResource(R.drawable.ic_gender_male);
                    }
                }
            } else {
                Toast.makeText(this, R.string.profile_load_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImage(Uri imageUri, String folder) {
        String userId = authRepository.getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(folder + "/" + userId + ".jpg");

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                updateUserField(folder.equals("avatars") ? "avatarUrl" : "bannerUrl", downloadUrl);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Tải ảnh lên thất bại", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserField(String field, String url) {
        String userId = authRepository.getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update(field, url)
                .addOnSuccessListener(aVoid -> {
                    if (field.equals("avatarUrl")) {
                        Glide.with(this).load(url).circleCrop().into(imgAvatar);
                        Toast.makeText(this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
