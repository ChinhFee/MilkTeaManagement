package com.example.milkteamanagement;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.adapters.AdminProductAdapter;
import com.example.milkteamanagement.adapters.AdminToppingAdapter;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.models.Topping;
import com.example.milkteamanagement.repositories.MenuRepository;
import com.example.milkteamanagement.repositories.ToppingRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class ManageMenuActivity extends AppCompatActivity {

    private RecyclerView rvItems;
    private TabLayout tabLayout;
    private TextView tvListTitle;
    private View btnBack;
    private View fabAdd;

    private MenuRepository menuRepository;
    private ToppingRepository toppingRepository;
    
    private AdminProductAdapter productAdapter;
    private AdminToppingAdapter toppingAdapter;
    
    private List<Product> productList = new ArrayList<>();
    private List<Topping> toppingList = new ArrayList<>();

    private boolean isToppingTab = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_menu);

        initViews();
        initRepositories();
        setupRecyclerView();
        setupTabs();
        loadData();

        btnBack.setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void initViews() {
        rvItems = findViewById(R.id.rvProducts);
        tabLayout = findViewById(R.id.tabLayout);
        tvListTitle = findViewById(R.id.tvListTitle);
        btnBack = findViewById(R.id.btnBack);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void initRepositories() {
        menuRepository = new MenuRepository();
        toppingRepository = new ToppingRepository();
    }

    private void setupRecyclerView() {
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        
        productAdapter = new AdminProductAdapter(productList, new AdminProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                showEditProductDialog(product);
            }

            @Override
            public void onDelete(Product product) {
                confirmDeleteProduct(product);
            }
        });

        toppingAdapter = new AdminToppingAdapter(toppingList, new AdminToppingAdapter.OnToppingActionListener() {
            @Override
            public void onEdit(Topping topping) {
                showEditToppingDialog(topping);
            }

            @Override
            public void onDelete(Topping topping) {
                confirmDeleteTopping(topping);
            }
        });

        rvItems.setAdapter(productAdapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isToppingTab = (tab.getPosition() == 1);
                updateUIForTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateUIForTab() {
        if (isToppingTab) {
            tvListTitle.setText("Danh sách Topping");
            rvItems.setAdapter(toppingAdapter);
        } else {
            tvListTitle.setText("Danh sách món");
            rvItems.setAdapter(productAdapter);
        }
    }

    private void loadData() {
        menuRepository.getProductsRealtime(new MenuRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                productList.clear();
                productList.addAll(products);
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ManageMenuActivity.this, "Lỗi tải sản phẩm: " + message, Toast.LENGTH_SHORT).show();
            }
        });

        toppingRepository.getToppingsRealtime(new ToppingRepository.ToppingListCallback() {
            @Override
            public void onSuccess(List<Topping> toppings) {
                toppingList.clear();
                toppingList.addAll(toppings);
                toppingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ManageMenuActivity.this, "Lỗi tải topping: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        if (isToppingTab) {
            showEditToppingDialog(null);
        } else {
            showEditProductDialog(null);
        }
    }

    private void showEditProductDialog(Product product) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_product, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextInputEditText edtName = view.findViewById(R.id.edtProductName);
        TextInputEditText edtPrice = view.findViewById(R.id.edtProductPrice);
        TextInputEditText edtCategory = view.findViewById(R.id.edtProductCategory);
        TextInputEditText edtImageUrl = view.findViewById(R.id.edtProductImageUrl);
        CheckBox cbAvailable = view.findViewById(R.id.cbIsAvailable);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (product != null) {
            tvTitle.setText("Chỉnh sửa món");
            edtName.setText(product.getName());
            edtPrice.setText(String.valueOf(product.getPrice()));
            edtCategory.setText(product.getCategory());
            edtImageUrl.setText(product.getImageUrl());
            cbAvailable.setChecked(product.isAvailable());
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String priceStr = edtPrice.getText().toString().trim();
            String category = edtCategory.getText().toString().trim();
            String imageUrl = edtImageUrl.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            int price = Integer.parseInt(priceStr);
            Product p = (product == null) ? new Product() : product;
            p.setName(name);
            p.setPrice(price);
            p.setCategory(category);
            p.setImageUrl(imageUrl);
            p.setAvailable(cbAvailable.isChecked());

            menuRepository.upsertProduct(p, new ToppingRepository.ToppingCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ManageMenuActivity.this, "Đã lưu thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(ManageMenuActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showEditToppingDialog(Topping topping) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_topping, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvDialogToppingTitle);
        TextInputEditText edtName = view.findViewById(R.id.edtToppingName);
        TextInputEditText edtPrice = view.findViewById(R.id.edtToppingPrice);
        CheckBox cbAvailable = view.findViewById(R.id.cbToppingAvailable);
        Button btnSave = view.findViewById(R.id.btnSaveTopping);
        Button btnCancel = view.findViewById(R.id.btnCancelTopping);

        if (topping != null) {
            tvTitle.setText("Chỉnh sửa Topping");
            edtName.setText(topping.getName());
            edtPrice.setText(String.valueOf(topping.getPrice()));
            cbAvailable.setChecked(topping.isAvailable());
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String priceStr = edtToppingPrice(edtPrice);

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            int price = Integer.parseInt(priceStr);
            Topping t = (topping == null) ? new Topping() : topping;
            t.setName(name);
            t.setPrice(price);
            t.setAvailable(cbAvailable.isChecked());

            toppingRepository.upsertTopping(t, new ToppingRepository.ToppingCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ManageMenuActivity.this, "Đã lưu thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(ManageMenuActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private String edtToppingPrice(TextInputEditText edtPrice) {
        return edtPrice.getText().toString().trim();
    }

    private void confirmDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa món")
                .setMessage("Bạn có chắc chắn muốn xóa món '" + product.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    menuRepository.deleteProduct(product.getId(), new ToppingRepository.ToppingCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ManageMenuActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String message) {
                            Toast.makeText(ManageMenuActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteTopping(Topping topping) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa Topping")
                .setMessage("Bạn có chắc chắn muốn xóa topping '" + topping.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    toppingRepository.deleteTopping(topping.getId(), new ToppingRepository.ToppingCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(ManageMenuActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String message) {
                            Toast.makeText(ManageMenuActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
