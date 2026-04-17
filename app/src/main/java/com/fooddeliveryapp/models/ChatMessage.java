package com.fooddeliveryapp.models;

import java.util.Date;

public class ChatMessage {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private String content;
    private String senderRole; // "CUSTOMER", "MERCHANT", "SYSTEM"
    private Date timestamp;

    public ChatMessage() {}

    public ChatMessage(Long userId, Long restaurantId, String content, String senderRole) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.content = content;
        this.senderRole = senderRole;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
