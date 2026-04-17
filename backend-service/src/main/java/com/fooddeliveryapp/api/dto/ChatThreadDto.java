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
}
