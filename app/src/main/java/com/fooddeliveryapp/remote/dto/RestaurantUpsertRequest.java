package com.fooddeliveryapp.remote.dto;

public class RestaurantUpsertRequest {
    public long ownerId;
    public String name;
    public String description;
    public String imageUrl;
    public String imagePublicId;
    public String address;
    public String phone;
    public String category;
    public int deliveryTime = 0;
    public double deliveryFee = 0.0;
    public double rating = 0.0;

    public RestaurantUpsertRequest(long ownerId, String name, String description, String imageUrl, String imagePublicId,
                                  String address, String phone, String category) {
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.imagePublicId = imagePublicId;
        this.address = address;
        this.phone = phone;
        this.category = category;
    }
}

