package com.example.milkteamanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milkteamanagement.models.User;
import com.example.milkteamanagement.repositories.AuthRepository;
import com.example.milkteamanagement.repositories.ProfileRepository;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave, btnCancel;
    private AuthRepository authRepository;
    private ProfileRepository profileRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etEditName);
        etPhone = findViewById(R.id.etEditPhone);
        etAddress = findViewById(R.id.etEditAddress);
        rgGender = findViewById(R.id.rgEditGender);
        rbMale = findViewById(R.id.rbEditMale);
        rbFemale = findViewById(R.id.rbEditFemale);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnCancel = findViewById(R.id.btnCancelEdit);

        authRepository = AuthRepository.getInstance();
        profileRepository = ProfileRepository.getInstance();

        loadCurrentData();

        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCurrentData() {
        authRepository.getUserData().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentUser = task.getResult();
                etName.setText(currentUser.getFullName());
                etPhone.setText(currentUser.getPhoneNumber());
                etAddress.setText(currentUser.getAddress());
                
                if ("Nữ".equalsIgnoreCase(currentUser.getGender())) {
                    rbFemale.setChecked(true);
                } else {
                    rbMale.setChecked(true);
                }
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String gender = rbFemale.isChecked() ? "Nữ" : "Nam";

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setFullName(name);
        currentUser.setPhoneNumber(phone);
        currentUser.setAddress(address);
        currentUser.setGender(gender);

        btnSave.setEnabled(false);
        profileRepository.updateUserProfile(currentUser).addOnCompleteListener(task -> {
            btnSave.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
