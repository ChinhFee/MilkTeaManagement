package com.example.milkteamanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.models.User;
import com.example.milkteamanagement.repositories.AuthRepository;
import com.example.milkteamanagement.repositories.CartManager;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.example.milkteamanagement.repositories.OrderRepository;
import com.example.milkteamanagement.repositories.ProfileRepository;
import java.text.DecimalFormat;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress, etNote;
    private RadioGroup rgPayment;
    private TextView tvSubtotal, tvTotal;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;
    
    private CartManager cartManager;
    private OrderRepository orderRepository;
    private ProfileRepository profileRepository;
    private AuthRepository authRepository;
    
    private DecimalFormat formatter = new DecimalFormat("#,###đ");
    private double shippingFee = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartManager = CartManager.getInstance();
        orderRepository = new OrderRepository();
        profileRepository = ProfileRepository.getInstance();
        authRepository = AuthRepository.getInstance();

        initViews();
        loadUserInfo();
        setupOrderSummary();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etNote = findViewById(R.id.etNote);
        rgPayment = findViewById(R.id.rgPayment);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadUserInfo() {
        String uid = authRepository.getCurrentUser().getUid();
        profileRepository.getUserProfile(uid).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    etName.setText(user.getFullName());
                    etPhone.setText(user.getPhoneNumber());
                    etAddress.setText(user.getAddress());
                }
            }
        }).addOnFailureListener(e -> {
        });
    }

    private void setupOrderSummary() {
        double subtotal = cartManager.getTotalCartPrice();
        double total = subtotal + shippingFee;
        
        tvSubtotal.setText(formatter.format(subtotal));
        tvTotal.setText(formatter.format(total));
    }

    private void handlePlaceOrder() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        
        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = rgPayment.getCheckedRadioButtonId() == R.id.rbCash 
                ? FirebaseConstants.PAYMENT_CASH : FirebaseConstants.PAYMENT_BANK_TRANSFER;

        Order order = new Order();
        order.setCustomerId(authRepository.getCurrentUser().getUid());
        order.setCustomerName(name);
        order.setCustomerPhone(phone);
        order.setCustomerAddress(address);
        order.setNote(note);
        order.setItems(cartManager.getCartItems());
        order.setTotalAmount(cartManager.getTotalCartPrice() + shippingFee);
        order.setPaymentMethod(paymentMethod);

        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        orderRepository.placeOrder(order, new OrderRepository.OrderActionCallback() {
            @Override
            public void onSuccess() {
                new FirebaseService(CheckoutActivity.this).sendNotificationToTopic(
                        "admins",
                        "Don hang moi",
                        "Khach " + name + " vua dat don hang moi."
                );
                progressBar.setVisibility(View.GONE);
                cartManager.clearCart();
                Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                finishAffinity();
                startActivity(new android.content.Intent(CheckoutActivity.this, MainActivity.class));
            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
