package com.example.milkteamanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.repositories.AuthRepository;
import com.example.milkteamanagement.repositories.CartManager;
import com.example.milkteamanagement.repositories.FirebaseConstants;
import com.example.milkteamanagement.repositories.OrderRepository;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private EditText edtName, edtPhone, edtAddress, edtNote;
    private TextView tvTotal;
    private Button btnConfirm;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        orderRepository = new OrderRepository();

        edtName = findViewById(R.id.edtCustomerName);
        edtPhone = findViewById(R.id.edtCustomerPhone);
        edtAddress = findViewById(R.id.edtCustomerAddress);
        edtNote = findViewById(R.id.edtOrderNote);
        tvTotal = findViewById(R.id.tvTotalAmount);
        btnConfirm = findViewById(R.id.btnConfirmCheckout);

        double total = CartManager.getInstance().getTotalCartPrice();
        tvTotal.setText(String.format("Tổng tiền: %,.0f VNĐ", total));

        btnConfirm.setOnClickListener(v -> processCheckout());
    }

    private void processCheckout() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String note = edtNote.getText().toString();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = AuthRepository.getInstance().getCurrentUser() != null 
                        ? AuthRepository.getInstance().getCurrentUser().getUid() 
                        : "guest_" + UUID.randomUUID().toString();

        Order order = new Order();
        order.setCustomerId(userId);
        order.setCustomerName(name);
        order.setCustomerPhone(phone);
        order.setCustomerAddress(address);
        order.setNote(note);
        order.setItems(CartManager.getInstance().getCartItems());
        order.setTotalAmount(CartManager.getInstance().getTotalCartPrice());
        order.setPaymentMethod(FirebaseConstants.PAYMENT_CASH);

        orderRepository.placeOrder(order, new OrderRepository.OrderActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(CheckoutActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
