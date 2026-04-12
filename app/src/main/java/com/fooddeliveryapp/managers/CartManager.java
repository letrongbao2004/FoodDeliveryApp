package com.fooddeliveryapp.managers;

import android.content.Context;
import com.fooddeliveryapp.models.CartItem;
import com.fooddeliveryapp.models.Food;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CartManager – Singleton providing high-level cart operations completely IN-MEMORY.
 * Backed by an ArrayList to completely bypass the deleted SQLite Database.
 */
public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartList;
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private CartManager(Context context) {
        cartList = new ArrayList<>();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    /** Add or merge food into cart. */
    public long addToCart(long userId, Food food, int qty,
                          String size, String spice, String addOns, String notes) {
        
        // Merge logic if same food is added
        for (CartItem item : cartList) {
            if (item.getFood() != null && item.getFood().getId() == food.getId() && item.getUserId() == userId) {
                // If it's literally the same configuration, increase quantity
                item.setQuantity(item.getQuantity() + qty);
                return item.getId();
            }
        }

        // Add new item
        CartItem newItem = new CartItem();
        newItem.setId(idGenerator.getAndIncrement()); // In-memory ID simulation
        newItem.setUserId(userId);
        newItem.setFood(food);
        newItem.setQuantity(qty);
        newItem.setSpecialNotes(notes);
        newItem.setSelectedSize(size);
        newItem.setSelectedSpice(spice);
        newItem.setSelectedAddOns(addOns);
        
        cartList.add(newItem);
        return newItem.getId();
    }

    /** Remove a specific cart item. */
    public boolean removeFromCart(int cartItemId) {
        Iterator<CartItem> iterator = cartList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == cartItemId) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /** Update quantity; if qty <= 0 the item is removed. */
    public boolean updateQuantity(int cartItemId, int newQty) {
        if (newQty <= 0) {
            return removeFromCart(cartItemId);
        }
        for (CartItem item : cartList) {
            if (item.getId() == cartItemId) {
                item.setQuantity(newQty);
                return true;
            }
        }
        return false;
    }

    /** Increment quantity by 1. */
    public boolean incrementQuantity(int cartItemId, int currentQty) {
        return updateQuantity(cartItemId, currentQty + 1);
    }

    /** Decrement quantity by 1; removes item if qty reaches 0. */
    public boolean decrementQuantity(int cartItemId, int currentQty) {
        return updateQuantity(cartItemId, currentQty - 1);
    }

    /** Get all cart items for a user. */
    public List<CartItem> getCartItems(long userId) {
        List<CartItem> userCart = new ArrayList<>();
        for (CartItem item : cartList) {
            if (item.getUserId() == userId) {
                userCart.add(item);
            }
        }
        return userCart;
    }

    /** Total item count (sum of quantities). */
    public int getCartCount(long userId) {
        int count = 0;
        for (CartItem item : getCartItems(userId)) {
            count += item.getQuantity();
        }
        return count;
    }

    /** Subtotal across all items. */
    public double getSubtotal(long userId) {
        double total = 0;
        for (CartItem item : getCartItems(userId)) {
            total += item.getTotalPrice();
        }
        return total;
    }

    /** Clear cart after successful order placement. */
    public void clearCart(long userId) {
        Iterator<CartItem> iterator = cartList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUserId() == userId) {
                iterator.remove();
            }
        }
    }

    /** Formatted subtotal string. */
    public String getSubtotalText(long userId) {
        return String.format("$%.2f", getSubtotal(userId));
    }
}
