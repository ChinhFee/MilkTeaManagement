package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.Topping;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class ToppingRepository {
    private final FirebaseFirestore db;

    public interface ToppingCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface ToppingListCallback {
        void onSuccess(List<Topping> toppings);
        void onFailure(String message);
    }

    public ToppingRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy danh sách Topping (Realtime)
     */
    public void getToppingsRealtime(ToppingListCallback callback) {
        db.collection(FirebaseConstants.COL_TOPPINGS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Topping> toppings = value.toObjects(Topping.class);
                        callback.onSuccess(toppings);
                    }
                });
    }

    /**
     * Thêm hoặc cập nhật Topping
     */
    public void upsertTopping(Topping topping, ToppingCallback callback) {
        String id = (topping.getId() == null || topping.getId().isEmpty())
                ? db.collection(FirebaseConstants.COL_TOPPINGS).document().getId()
                : topping.getId();
        topping.setId(id);

        db.collection(FirebaseConstants.COL_TOPPINGS).document(id).set(topping)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
