package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class MenuRepository {
    private final FirebaseFirestore db;

    public interface MenuCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onFailure(String message);
    }

    public MenuRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy danh sách sản phẩm (Realtime)
     */
    public void getProductsRealtime(ProductListCallback callback) {
        db.collection(FirebaseConstants.COL_PRODUCTS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Product> products = value.toObjects(Product.class);
                        callback.onSuccess(products);
                    }
                });
    }

    /**
     * Lấy sản phẩm theo danh mục
     */
    public void getProductsByCategory(String category, ProductListCallback callback) {
        db.collection(FirebaseConstants.COL_PRODUCTS)
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = queryDocumentSnapshots.toObjects(Product.class);
                    callback.onSuccess(products);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void upsertProduct(Product product, MenuCallback callback) {
        if (product == null) {
            callback.onFailure("Sản phẩm không hợp lệ");
            return;
        }

        String id = (product.getId() == null || product.getId().isEmpty())
                ? db.collection(FirebaseConstants.COL_PRODUCTS).document().getId()
                : product.getId();
        product.setId(id);

        db.collection(FirebaseConstants.COL_PRODUCTS).document(id).set(product)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteProduct(String productId, MenuCallback callback) {
        db.collection(FirebaseConstants.COL_PRODUCTS).document(productId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}