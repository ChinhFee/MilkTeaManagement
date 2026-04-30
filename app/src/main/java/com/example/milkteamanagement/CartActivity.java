package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.repositories.CartManager;
import java.text.DecimalFormat;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private TextView tvTotalPrice;
    private LinearLayout llEmptyCart, llBottom;
    private Button btnCheckout;
    private CartManager cartManager;
    private DecimalFormat formatter = new DecimalFormat("#,###đ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance();

        initViews();
        setupRecyclerView();
        updateUI();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnClearCart).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Xóa giỏ hàng")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả món trong giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    cartManager.clearCart();
                    updateUI();
                    Toast.makeText(this, "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });

        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        llEmptyCart = findViewById(R.id.llEmptyCart);
        llBottom = findViewById(R.id.llBottom);
        btnCheckout = findViewById(R.id.btnCheckout);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartManager.getCartItems(), this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);
    }

    private void updateUI() {
        if (cartManager.getCartItems().isEmpty()) {
            llEmptyCart.setVisibility(View.VISIBLE);
            llBottom.setVisibility(View.GONE);
            rvCart.setVisibility(View.GONE);
        } else {
            llEmptyCart.setVisibility(View.GONE);
            llBottom.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.VISIBLE);
            tvTotalPrice.setText(formatter.format(cartManager.getTotalCartPrice()));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onQuantityChanged() {
        tvTotalPrice.setText(formatter.format(cartManager.getTotalCartPrice()));
    }

    @Override
    public void onItemRemoved(int position) {
        new AlertDialog.Builder(this)
            .setTitle("Xóa món ăn")
            .setMessage("Bạn muốn bỏ món này khỏi giỏ hàng?")
            .setPositiveButton("Đồng ý", (dialog, which) -> {
                cartManager.removeFromCart(position);
                updateUI();
            })
            .setNegativeButton("Hủy", (dialog, which) -> adapter.notifyItemChanged(position))
            .show();
    }
}
