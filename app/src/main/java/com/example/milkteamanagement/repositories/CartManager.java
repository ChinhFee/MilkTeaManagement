package com.example.milkteamanagement.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.milkteamanagement.models.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREF_NAME = "MilkTeaCart";
    private static final String KEY_CART = "cart_items";
    private static CartManager instance;
    private List<CartItem> cartItems;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    private CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCart();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    // Overloaded for backward compatibility where context might be tricky, 
    // but better to always pass context.
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CartManager must be initialized with context first");
        }
        return instance;
    }

    private void saveCart() {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(KEY_CART, json).apply();
    }

    private void loadCart() {
        String json = sharedPreferences.getString(KEY_CART, null);
        if (json == null) {
            cartItems = new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
            cartItems = gson.fromJson(json, type);
        }
    }

    public void addToCart(CartItem newItem) {
        boolean found = false;
        for (CartItem item : cartItems) {
            if (isSameConfig(item, newItem)) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                item.calculateSubTotal();
                found = true;
                break;
            }
        }
        if (!found) {
            cartItems.add(newItem);
        }
        saveCart();
    }

    public void removeFromCart(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            saveCart();
        }
    }

    public void clearCart() {
        cartItems.clear();
        saveCart();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalCartPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubTotal();
        }
        return total;
    }

    private boolean isSameConfig(CartItem item1, CartItem item2) {
        if (!item1.getProductId().equals(item2.getProductId())) return false;
        if (!item1.getSize().equals(item2.getSize())) return false;
        
        // So sánh danh sách Topping (Tên và số lượng phải khớp)
        if (item1.getToppings() == null && item2.getToppings() == null) return true;
        if (item1.getToppings() == null || item2.getToppings() == null) return false;
        if (item1.getToppings().size() != item2.getToppings().size()) return false;
        
        for (int i = 0; i < item1.getToppings().size(); i++) {
            if (!item1.getToppings().get(i).getId().equals(item2.getToppings().get(i).getId())) {
                return false;
            }
        }
        return true;
    }
}
