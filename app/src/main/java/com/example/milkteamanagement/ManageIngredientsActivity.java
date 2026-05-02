package com.example.milkteamanagement;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.adapters.IngredientAdapter;
import com.example.milkteamanagement.models.Ingredient;
import com.example.milkteamanagement.repositories.IngredientRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageIngredientsActivity extends AppCompatActivity {

    private RecyclerView rvIngredients;
    private IngredientAdapter adapter;
    private IngredientRepository repository;
    private List<Ingredient> ingredientList;
    private Button btnInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ingredients);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý nguyên liệu");
        }

        rvIngredients = findViewById(R.id.rvIngredients);
        btnInit = findViewById(R.id.btnInitIngredients);
        
        repository = new IngredientRepository();
        ingredientList = new ArrayList<>();
        adapter = new IngredientAdapter(ingredientList);

        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(adapter);

        loadIngredients();

        btnInit.setOnClickListener(v -> {
            repository.initBasicIngredients();
            Toast.makeText(this, "Đang khởi tạo dữ liệu mẫu...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadIngredients() {
        repository.getIngredientsRef().addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                ingredientList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Ingredient ingredient = doc.toObject(Ingredient.class);
                    if (ingredient != null) {
                        ingredientList.add(ingredient);
                    }
                }
                adapter.setIngredients(ingredientList);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
