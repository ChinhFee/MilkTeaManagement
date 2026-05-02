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
import com.example.milkteamanagement.adapters.RestockAdapter;
import com.example.milkteamanagement.models.Material;
import com.example.milkteamanagement.repositories.MaterialRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestockActivity extends AppCompatActivity {

    private RecyclerView rvRestock;
    private RestockAdapter adapter;
    private MaterialRepository repository;
    private List<Material> materialList;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restock);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nhập thêm hàng");
        }

        rvRestock = findViewById(R.id.rvRestock);
        btnConfirm = findViewById(R.id.btnConfirmRestock);

        repository = new MaterialRepository();
        materialList = new ArrayList<>();
        adapter = new RestockAdapter(materialList);

        rvRestock.setLayoutManager(new LinearLayoutManager(this));
        rvRestock.setAdapter(adapter);

        loadMaterials();

        btnConfirm.setOnClickListener(v -> {
            performRestock();
        });
    }

    private void loadMaterials() {
        repository.getMaterialsRef().get().addOnSuccessListener(queryDocumentSnapshots -> {
            materialList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                Material material = doc.toObject(Material.class);
                if (material != null) {
                    materialList.add(material);
                }
            }
            adapter.setMaterials(materialList);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void performRestock() {
        Map<String, Double> restockMap = adapter.getRestockQuantities();
        if (restockMap.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn số lượng cần nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Task<Void>> tasks = new ArrayList<>();
        for (Map.Entry<String, Double> entry : restockMap.entrySet()) {
            if (entry.getValue() > 0) {
                tasks.add(repository.updateStock(entry.getKey(), entry.getValue()));
            }
        }

        if (tasks.isEmpty()) {
            Toast.makeText(this, "Chưa nhập số lượng cho bất kỳ mặt hàng nào", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        Tasks.whenAll(tasks).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Nhập hàng thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            btnConfirm.setEnabled(true);
            Toast.makeText(this, "Lỗi nhập hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
