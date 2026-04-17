package com.fooddeliveryapp.api.dto;

import lombok.Data;
import java.util.Date;

@Data
public class ChatThreadDto {
    private Long userId;
    private Long restaurantId;
    private String participantName;
    private String participantImageUrl;
    private String lastMessage;
    private Date lastMessageTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public String getParticipantImageUrl() { return participantImageUrl; }
    public void setParticipantImageUrl(String participantImageUrl) { this.participantImageUrl = participantImageUrl; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public Date getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(Date lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
