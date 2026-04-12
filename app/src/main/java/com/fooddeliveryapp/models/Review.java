package com.fooddeliveryapp.models;

public class Review {
    private int id;
    private int userId;
    private int restaurantId;
    private int orderId;
    private String userName;
    private float rating;
    private String comment;
    private long createdAt;

    public Review() {}

    public Review(int userId, int restaurantId, int orderId,
                  String userName, float rating, String comment) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.orderId = orderId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRestaurantId() { return restaurantId; }
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
