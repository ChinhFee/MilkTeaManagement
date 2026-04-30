package com.example.milkteamanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.models.Topping;
import com.example.milkteamanagement.repositories.CartManager;
import com.example.milkteamanagement.repositories.GoogleDriveRepository;
import com.example.milkteamanagement.repositories.ToppingRepository;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProductDetail;
    private TextView tvProductName, tvProductPrice, tvQuantity;
    private RadioGroup rgSize;
    private RadioButton rbSizeM, rbSizeL;
    private RecyclerView rvToppings;
    private Button btnAddToCart;
    private ImageButton btnDecrease, btnIncrease;

    private Product product;
    private int quantity = 1;
    private ToppingAdapter toppingAdapter;
    private final ToppingRepository toppingRepository = new ToppingRepository();
    private final GoogleDriveRepository driveRepository = new GoogleDriveRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        setupToolbar();
        loadProductData();
        setupToppingList();
        setupQuantityButtons();
        setupAddToCart();
    }

    private void initViews() {
        imgProductDetail = findViewById(R.id.imgProductDetail);
        tvProductName = findViewById(R.id.tvProductNameDetail);
        tvProductPrice = findViewById(R.id.tvProductPriceDetail);
        tvQuantity = findViewById(R.id.tvQuantity);
        rgSize = findViewById(R.id.rgSize);
        rbSizeM = findViewById(R.id.rbSizeM);
        rbSizeL = findViewById(R.id.rbSizeL);
        rvToppings = findViewById(R.id.rvToppings);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadProductData() {
        String productJson = getIntent().getStringExtra("product_json");
        if (productJson != null) {
            product = new Gson().fromJson(productJson, Product.class);
            tvProductName.setText(product.getName());
            tvProductPrice.setText(String.format(Locale.getDefault(), "%,d VNĐ", product.getPrice()));

            String directLink = driveRepository.convertToDirectLink(product.getImageUrl());
            Glide.with(this)
                    .load(directLink)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgProductDetail);
        }
    }

    private void setupToppingList() {
        rvToppings.setLayoutManager(new LinearLayoutManager(this));
        toppingRepository.getToppingsRealtime(new ToppingRepository.ToppingListCallback() {
            @Override
            public void onSuccess(List<Topping> toppings) {
                List<Topping> availableToppings = new ArrayList<>();
                for (Topping t : toppings) {
                    if (t.isAvailable()) availableToppings.add(t);
                }
                toppingAdapter = new ToppingAdapter(availableToppings);
                rvToppings.setAdapter(toppingAdapter);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi tải topping: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupQuantityButtons() {
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });
    }

    private void setupAddToCart() {
        btnAddToCart.setOnClickListener(v -> {
            String size = rbSizeM.isChecked() ? "M" : "L";
            List<Topping> selectedToppings = toppingAdapter.getSelectedToppings();

            CartItem cartItem = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    quantity,
                    size,
                    selectedToppings,
                    product.getPrice()
            );

            CartManager.getInstance().addToCart(cartItem);
            Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}