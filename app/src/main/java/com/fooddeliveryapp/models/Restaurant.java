package com.fooddeliveryapp.models;

import com.google.gson.annotations.SerializedName;

public class Restaurant {
    private int id;
    private String name;
    private String description;
    private String address;
    private String imageUrl;
    private String category;
    private double rating;
    private int reviewCount;
    private double deliveryFee;
    private int deliveryTime; // minutes
    private double minOrder;
    private double distance; // km

    @SerializedName("isOpen")
    private boolean isOpen;
    private boolean isFeatured;
    private boolean hasFreeDelivery;
    private boolean hasPromo;
    private int ownerId;
    private String phone;
    private String openHours;

    public Restaurant() {
    }

    public Restaurant(String name, String description, String address, String imageUrl,
            String category, double rating, double deliveryFee,
            int deliveryTime, double distance, boolean isOpen) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.imageUrl = imageUrl;
        this.category = category;
        this.rating = rating;
        this.deliveryFee = deliveryFee;
        this.deliveryTime = deliveryTime;
        this.distance = distance;
        this.isOpen = isOpen;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(int deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public double getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(double minOrder) {
        this.minOrder = minOrder;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public boolean isHasFreeDelivery() {
        return hasFreeDelivery;
    }

    public void setHasFreeDelivery(boolean hasFreeDelivery) {
        this.hasFreeDelivery = hasFreeDelivery;
    }

    public boolean isHasPromo() {
        return hasPromo;
    }

    public void setHasPromo(boolean hasPromo) {
        this.hasPromo = hasPromo;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOpenHours() {
        return openHours;
    }

    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }

    public String getDeliveryTimeText() {
        return deliveryTime + " mins";
    }

    public String getDistanceText() {
        return String.format("%.1f km", distance);
    }

    public String getRatingText() {
        return String.format("%.1f", rating);
    }
}