package com.example.milkteamanagement;

import android.os.Bundle;
import android.util.Log;
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
    private TextView tvProductName, tvProductPrice, tvQuantity, tvTotalPriceBottom;
    private RadioGroup rgSize, rgSugar, rgIce;
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
        tvTotalPriceBottom = findViewById(R.id.tvTotalPriceBottom);
        rgSize = findViewById(R.id.rgSize);
        rgSugar = findViewById(R.id.rgSugar);
        rgIce = findViewById(R.id.rgIce);
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
            try {
                product = new Gson().fromJson(productJson, Product.class);
            } catch (Exception e) {
                Log.e("ProductDetail", "Lỗi parse JSON product: " + e.getMessage());
            }
        }

        if (product != null) {
            tvProductName.setText(product.getName() != null ? product.getName() : "Sản phẩm không tên");
            
            try {
                tvProductPrice.setText(String.format(Locale.getDefault(), "%,d VNĐ", product.getPrice()));
            } catch (Exception e) {
                tvProductPrice.setText("Liên hệ");
            }

            updateTotalPrice();

            rgSize.setOnCheckedChangeListener((group, checkedId) -> updateTotalPrice());

            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String directLink = driveRepository.convertToDirectLink(imageUrl);
                Glide.with(this)
                        .load(directLink)
                        .placeholder(R.drawable.img_placeholder)
                        .error(R.drawable.img_placeholder)
                        .into(imgProductDetail);
            } else {
                imgProductDetail.setImageResource(R.drawable.img_placeholder);
            }
        } else {
            Toast.makeText(this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
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
                toppingAdapter = new ToppingAdapter(availableToppings, () -> updateTotalPrice());
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
            updateTotalPrice();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });
    }

    private void updateTotalPrice() {
        if (product == null) return;
        
        double basePrice = product.getPrice();
        double sizePrice = rbSizeL.isChecked() ? 5000 : 0;
        
        double toppingPrice = 0;
        if (toppingAdapter != null) {
            for (Topping t : toppingAdapter.getSelectedToppings()) {
                toppingPrice += t.getPrice();
            }
        }
        
        double total = (basePrice + sizePrice + toppingPrice) * quantity;
        tvTotalPriceBottom.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", total));
    }

    private void setupAddToCart() {
        btnAddToCart.setOnClickListener(v -> {
            String size = rbSizeM.isChecked() ? "M" : "L";
            
            int sugarId = rgSugar.getCheckedRadioButtonId();
            String sugar = "50%";
            if (sugarId == R.id.sugar0) sugar = "0%";
            else if (sugarId == R.id.sugar30) sugar = "30%";
            else if (sugarId == R.id.sugar50) sugar = "50%";
            else if (sugarId == R.id.sugar70) sugar = "70%";
            else if (sugarId == R.id.sugar100) sugar = "100%";

            int iceId = rgIce.getCheckedRadioButtonId();
            String ice = "50%";
            if (iceId == R.id.ice0) ice = "0%";
            else if (iceId == R.id.ice30) ice = "30%";
            else if (iceId == R.id.ice50) ice = "50%";
            else if (iceId == R.id.ice70) ice = "70%";
            else if (iceId == R.id.ice100) ice = "100%";

            List<Topping> selectedToppings = toppingAdapter.getSelectedToppings();

            CartItem cartItem = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    quantity,
                    size,
                    sugar,
                    ice,
                    selectedToppings,
                    product.getPrice()
            );

            CartManager.getInstance().addToCart(cartItem);
            Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}