package com.fooddeliveryapp.models;

import com.fooddeliveryapp.utils.AppUtils;

public class CartItem {
    private int id;
    private long userId;
    private Food food;
    private int quantity;
    private String specialNotes;
    private String selectedSize; // Small / Medium / Large
    private String selectedSpice; // Mild / Medium / Hot
    private String selectedAddOns; // comma-separated

    public CartItem() {
    }

    public CartItem(long userId, Food food, int quantity) {
        this.userId = userId;
        this.food = food;
        this.quantity = quantity;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSpecialNotes() {
        return specialNotes;
    }

    public void setSpecialNotes(String specialNotes) {
        this.specialNotes = specialNotes;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public String getSelectedSpice() {
        return selectedSpice;
    }

    public void setSelectedSpice(String selectedSpice) {
        this.selectedSpice = selectedSpice;
    }

    public String getSelectedAddOns() {
        return selectedAddOns;
    }

    public void setSelectedAddOns(String selectedAddOns) {
        this.selectedAddOns = selectedAddOns;
    }

    public double getTotalPrice() {
        return food != null ? food.getPrice() * quantity : 0;
    }

    public String getTotalPriceText() {
        return AppUtils.formatPrice(getTotalPrice());
    }
}