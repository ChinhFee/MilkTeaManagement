package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Material;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.models.RecipeItem;
import com.example.milkteamanagement.models.Topping;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MaterialRepository {
    private final FirebaseFirestore db;
    private final CollectionReference materialsRef;
    private final CollectionReference productsRef;
    private final CollectionReference toppingsRef;

    public MaterialRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.materialsRef = db.collection("materials");
        this.productsRef = db.collection("products");
        this.toppingsRef = db.collection("toppings");
    }

    public Task<Void> addMaterial(Material material) {
        if (material.getId() == null) {
            material.setId(materialsRef.document().getId());
        }
        return materialsRef.document(material.getId()).set(material);
    }

    public Task<Void> updateStock(String materialId, double addedQuantity) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(materialsRef.document(materialId));
            double currentQty = 0;
            if (snapshot.exists()) {
                Double val = snapshot.getDouble("quantity");
                if (val != null) currentQty = val;
            }
            transaction.update(materialsRef.document(materialId), "quantity", currentQty + addedQuantity);
            return null;
        });
    }

    public Task<Void> deductMaterialsFromOrder(Order order) {
        return db.runTransaction(transaction -> {
            Map<String, Double> materialDeductions = new HashMap<>();

            if (order.getItems() != null) {
                for (CartItem item : order.getItems()) {
                    // Trừ nguyên liệu cho món chính
                    DocumentSnapshot productDoc = transaction.get(productsRef.document(item.getProductId()));
                    Product product = productDoc.toObject(Product.class);
                    if (product != null && product.getRecipe() != null) {
                        for (RecipeItem recipeItem : product.getRecipe()) {
                            String matId = recipeItem.getMaterialId();
                            double amount = recipeItem.getQuantity() * item.getQuantity();
                            materialDeductions.put(matId, materialDeductions.getOrDefault(matId, 0.0) + amount);
                        }
                    }

                    // Trừ nguyên liệu cho toppings
                    if (item.getToppings() != null) {
                        for (Topping t : item.getToppings()) {
                            DocumentSnapshot toppingDoc = transaction.get(toppingsRef.document(t.getId()));
                            Topping topping = toppingDoc.toObject(Topping.class);
                            if (topping != null && topping.getRecipe() != null) {
                                for (RecipeItem recipeItem : topping.getRecipe()) {
                                    String matId = recipeItem.getMaterialId();
                                    double amount = recipeItem.getQuantity() * item.getQuantity();
                                    materialDeductions.put(matId, materialDeductions.getOrDefault(matId, 0.0) + amount);
                                }
                            }
                        }
                    }
                }
            }

            // Thực hiện trừ kho cho tất cả nguyên liệu đã tổng hợp
            for (Map.Entry<String, Double> entry : materialDeductions.entrySet()) {
                String matId = entry.getKey();
                double amountToDeduct = entry.getValue();

                DocumentSnapshot matDoc = transaction.get(materialsRef.document(matId));
                double currentQty = 0;
                if (matDoc.exists()) {
                    Double val = matDoc.getDouble("quantity");
                    if (val != null) currentQty = val;
                }
                transaction.update(materialsRef.document(matId), "quantity", currentQty - amountToDeduct);
            }

            return null;
        });
    }

    public CollectionReference getMaterialsRef() {
        return materialsRef;
    }
}
