package com.example.milkteamanagement.repositories;

import android.util.Log;
import com.example.milkteamanagement.models.Product;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MenuRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference productsRef = db.collection(FirebaseConstants.COL_PRODUCTS);

    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onFailure(String message);
    }

    public void getProductsRealtime(ProductListCallback callback) {
        productsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                callback.onFailure(error.getMessage());
                return;
            }

            List<Product> products = new ArrayList<>();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            product.setId(doc.getId());
                            products.add(product);
                        }
                    } catch (Exception e) {
                        Log.e("MenuRepository", "Dữ liệu món " + doc.getId() + " bị lỗi định dạng: " + e.getMessage());
                    }
                }
            }
            callback.onSuccess(products);
        });
    }

    public void upsertProduct(Product product, ToppingRepository.ToppingCallback callback) {
        String id = (product.getId() == null || product.getId().isEmpty())
                ? productsRef.document().getId()
                : product.getId();
        product.setId(id);

        productsRef.document(id).set(product)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteProduct(String productId, ToppingRepository.ToppingCallback callback) {
        productsRef.document(productId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
