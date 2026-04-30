package com.example.milkteamanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milkteamanagement.repositories.AuthRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etAddress, etAdminCode, etPassword;
    private RadioGroup rgGender;
    private Button btnRegister;
    private TextView tvLogin;
    private AuthRepository authRepository;

    // Mã bảo vệ để đăng ký quyền Admin
    private static final String SECRET_ADMIN_CODE = "ADMIN123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etAdminCode = findViewById(R.id.etAdminCode);
        etPassword = findViewById(R.id.etPassword);
        rgGender = findViewById(R.id.rgGender);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        authRepository = AuthRepository.getInstance();

        tvLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> performRegister());
    }

    private void performRegister() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String adminCodeInput = etAdminCode.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        String gender = "Nam"; // Mặc định
        if (selectedGenderId == R.id.rbFemale) {
            gender = "Nữ";
        }

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xác định vai trò dựa trên mã Admin
        String tempRole = "customer";
        if (!adminCodeInput.isEmpty()) {
            if (SECRET_ADMIN_CODE.equals(adminCodeInput)) {
                tempRole = "admin";
            } else {
                Toast.makeText(this, "Mã Admin không chính xác!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final String role = tempRole;
        final String finalGender = gender;

        btnRegister.setEnabled(false);
        authRepository.register(email, password, name, phone, address, role, finalGender)
            .addOnCompleteListener(task -> {
                btnRegister.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Đăng ký " + role + " thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}
