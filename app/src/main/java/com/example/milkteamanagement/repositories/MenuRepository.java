package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.Product;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MenuRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference productsRef = db.collection("products");

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
                    Product product = doc.toObject(Product.class);
                    product.setId(doc.getId());
                    products.add(product);
                }
            }
            callback.onSuccess(products);
        });
    }
}
