package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Ingredient;
import com.example.milkteamanagement.models.Order;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.models.RecipeItem;
import com.example.milkteamanagement.models.Topping;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class IngredientRepository {
    private final FirebaseFirestore db;
    private final CollectionReference ingredientsRef;
    private final CollectionReference productsRef;
    private final CollectionReference toppingsRef;

    public IngredientRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.ingredientsRef = db.collection("ingredients");
        this.productsRef = db.collection("products");
        this.toppingsRef = db.collection("toppings");
    }

    // Thêm nguyên liệu mới hoặc cập nhật thông tin
    public Task<Void> saveIngredient(Ingredient ingredient) {
        if (ingredient.getId() == null || ingredient.getId().isEmpty()) {
            ingredient.setId(ingredientsRef.document().getId());
        }
        return ingredientsRef.document(ingredient.getId()).set(ingredient);
    }

    // Nhập thêm hàng (cộng dồn số lượng)
    public Task<Void> updateStock(String ingredientId, double addedQuantity) {
        return db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(ingredientsRef.document(ingredientId));
            double currentQty = 0;
            if (snapshot.exists()) {
                Double val = snapshot.getDouble("quantity");
                if (val != null) currentQty = val;
            }
            transaction.update(ingredientsRef.document(ingredientId), "quantity", currentQty + addedQuantity);
            return null;
        });
    }

    // Trừ kho khi có đơn hàng (Đã sửa lỗi getMaterialId)
    public Task<Void> deductIngredientsFromOrder(Order order) {
        if (order.getItems() == null) return Tasks.forResult(null);

        return db.runTransaction(transaction -> {
            for (CartItem item : order.getItems()) {
                DocumentSnapshot productDoc = transaction.get(productsRef.document(item.getProductId()));
                Product product = productDoc.toObject(Product.class);

                if (product != null && product.getRecipe() != null) {
                    for (RecipeItem ri : product.getRecipe()) {
                        deduct(transaction, ri.getMaterialId(), ri.getQuantity() * item.getQuantity());
                    }
                }

                if (item.getToppings() != null) {
                    for (Topping topping : item.getToppings()) {
                        DocumentSnapshot toppingDoc = transaction.get(toppingsRef.document(topping.getId()));
                        Topping fullTopping = toppingDoc.toObject(Topping.class);
                        if (fullTopping != null && fullTopping.getRecipe() != null) {
                            for (RecipeItem ri : fullTopping.getRecipe()) {
                                deduct(transaction, ri.getMaterialId(), ri.getQuantity() * item.getQuantity());
                            }
                        }
                    }
                }
            }
            return null;
        });
    }

    private void deduct(Transaction transaction, String ingId, double amount) throws FirebaseFirestoreException {
        DocumentSnapshot ingDoc = transaction.get(ingredientsRef.document(ingId));
        if (ingDoc.exists()) {
            double current = ingDoc.getDouble("quantity") != null ? ingDoc.getDouble("quantity") : 0;
            transaction.update(ingredientsRef.document(ingId), "quantity", Math.max(0, current - amount));
        }
    }

    public void initBasicIngredients() {
        List<Ingredient> basicIngredients = new ArrayList<>();
        basicIngredients.add(new Ingredient(null, "Trà đen", 5000, "g", 500));
        basicIngredients.add(new Ingredient(null, "Trà xanh", 5000, "g", 500));
        basicIngredients.add(new Ingredient(null, "Sữa đặc", 10000, "g", 1000));
        basicIngredients.add(new Ingredient(null, "Đường", 20000, "g", 2000));
        basicIngredients.add(new Ingredient(null, "Bột béo", 5000, "g", 500));
        basicIngredients.add(new Ingredient(null, "Trân châu đen", 3000, "g", 300));
        basicIngredients.add(new Ingredient(null, "Thạch trái cây", 2000, "g", 200));

        for (Ingredient ing : basicIngredients) {
            saveIngredient(ing);
        }
    }

    public CollectionReference getIngredientsRef() {
        return ingredientsRef;
    }
}
