package com.example.milkteamanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.adapters.MaterialAdapter;
import com.example.milkteamanagement.models.Material;
import com.example.milkteamanagement.repositories.MaterialRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageMaterialsActivity extends AppCompatActivity {

    private RecyclerView rvMaterials;
    private MaterialAdapter adapter;
    private MaterialRepository repository;
    private List<Material> materialList;
    private Button btnRestock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_materials);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý nguyên vật liệu");
        }

        rvMaterials = findViewById(R.id.rvMaterials);
        btnRestock = findViewById(R.id.btnRestock);
        
        repository = new MaterialRepository();
        materialList = new ArrayList<>();
        adapter = new MaterialAdapter(materialList);

        rvMaterials.setLayoutManager(new LinearLayoutManager(this));
        rvMaterials.setAdapter(adapter);

        loadMaterials();

        btnRestock.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestockActivity.class);
            startActivity(intent);
        });
    }

    private void loadMaterials() {
        repository.getMaterialsRef().addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                materialList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Material material = doc.toObject(Material.class);
                    if (material != null) {
                        materialList.add(material);
                    }
                }
                adapter.setMaterials(materialList);
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
