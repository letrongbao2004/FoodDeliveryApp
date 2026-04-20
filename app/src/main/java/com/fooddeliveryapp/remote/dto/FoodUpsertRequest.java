package com.fooddeliveryapp.remote.dto;

import com.google.gson.annotations.SerializedName;

public class FoodUpsertRequest {
    public RestaurantRef restaurant;
    public String name;
    public String description;
    public double price;
    public String imageUrl;
    public String imagePublicId;
    public String category;

    @SerializedName("available")
    public boolean available = true;

    @SerializedName("bestSeller")
    public boolean bestSeller = false;

    @SerializedName("new")
    public boolean isNew = false;

    public double rating = 0.0;
    public int orderCount = 0;
    public int stockQuantity = 0;

    public FoodUpsertRequest(long restaurantId, String name, String description, double price, String imageUrl, String imagePublicId, String category) {
        this.restaurant = new RestaurantRef(restaurantId);
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.imagePublicId = imagePublicId;
        this.category = category;
    }

    public static class RestaurantRef {
        public long id;

        public RestaurantRef(long id) {
            this.id = id;
        }
    }
}

