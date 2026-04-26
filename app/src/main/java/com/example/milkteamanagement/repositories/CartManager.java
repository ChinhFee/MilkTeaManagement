package com.example.milkteamanagement.repositories;

import com.example.milkteamanagement.models.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(CartItem newItem) {
        for (CartItem item : cartItems) {
            if (isSameConfig(item, newItem)) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                item.calculateSubTotal();
                return;
            }
        }
        cartItems.add(newItem);
    }

    public void removeFromCart(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
        }
    }

    public void clearCart() {
        cartItems.clear();
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
