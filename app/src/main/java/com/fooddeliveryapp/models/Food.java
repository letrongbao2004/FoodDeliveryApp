package com.fooddeliveryapp.models;

import com.google.gson.annotations.SerializedName;

public class Food {
    private int id;
    private int restaurantId;
    private Restaurant restaurant;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;

    // Backend serializes boolean fields with "is" prefix stripped:
    // isAvailable -> "available", isBestSeller -> "bestSeller", isNew -> "new"
    @SerializedName("available")
    private boolean isAvailable;

    @SerializedName("bestSeller")
    private boolean isBestSeller;

    @SerializedName("new")
    private boolean isNew;

    private double rating;
    private int orderCount;
    private int stockQuantity;

    public Food() {
    }

    public Food(int restaurantId, String name, String description, double price,
            String imageUrl, String category, boolean isAvailable) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.isAvailable = isAvailable;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRestaurantId() {
        return restaurant != null ? restaurant.getId() : restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public Restaurant getRestaurant() {
        return restaurant;
    }
    
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isBestSeller() {
        return isBestSeller;
    }

    public void setBestSeller(boolean bestSeller) {
        isBestSeller = bestSeller;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getPriceText() {
        return com.fooddeliveryapp.utils.AppUtils.formatPrice(price);
    }
}