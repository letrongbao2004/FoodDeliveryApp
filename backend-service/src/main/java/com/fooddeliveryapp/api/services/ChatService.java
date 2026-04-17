package com.fooddeliveryapp.api.services;

import com.fooddeliveryapp.api.models.ChatMessage;
import com.fooddeliveryapp.api.repositories.ChatMessageRepository;
import com.fooddeliveryapp.api.repositories.RestaurantRepository;
import com.fooddeliveryapp.api.repositories.UserRepository;
import com.fooddeliveryapp.api.dto.ChatThreadDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, SimpMessagingTemplate messagingTemplate, 
                       UserRepository userRepository, RestaurantRepository restaurantRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public void sendOrderConfirmation(Long userId, Long restaurantId, Long orderId) {
        String msg = "Hello! Your order #" + String.format("%05d", orderId) + " has been successfully placed. Thank you for your order!";
        ChatMessage chatMsg = new ChatMessage();
        chatMsg.setUserId(userId);
        chatMsg.setRestaurantId(restaurantId);
        chatMsg.setSenderRole("SYSTEM");
        chatMsg.setContent(msg);
        
        ChatMessage saved = chatMessageRepository.save(chatMsg);
        
        // Broadcast to specific user and restaurant pair topic
        String destination = "/topic/chat/" + userId + "_" + restaurantId;
        messagingTemplate.convertAndSend(destination, saved);
    }

    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        ChatMessage saved = chatMessageRepository.save(message);
        String destination = "/topic/chat/" + saved.getUserId() + "_" + saved.getRestaurantId();
        messagingTemplate.convertAndSend(destination, saved);
        return saved;
    }

    public List<ChatMessage> getHistory(Long userId, Long restaurantId) {
        return chatMessageRepository.findByUserIdAndRestaurantIdOrderByTimestampAsc(userId, restaurantId);
    }

    public List<ChatThreadDto> getUserThreads(Long userId) {
        List<ChatMessage> latestMsgs = chatMessageRepository.findLatestMessagesForUser(userId);
        return latestMsgs.stream().map(msg -> {
            ChatThreadDto dto = new ChatThreadDto();
            dto.setUserId(userId);
            dto.setRestaurantId(msg.getRestaurantId());
            dto.setLastMessage(msg.getContent());
            dto.setLastMessageTime(msg.getTimestamp());
            
            restaurantRepository.findById(msg.getRestaurantId()).ifPresent(r -> {
                dto.setParticipantName(r.getName());
                dto.setParticipantImageUrl(r.getImageUrl());
            });
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ChatThreadDto> getRestaurantThreads(Long restaurantId) {
        List<ChatMessage> latestMsgs = chatMessageRepository.findLatestMessagesForRestaurant(restaurantId);
        return latestMsgs.stream().map(msg -> {
            ChatThreadDto dto = new ChatThreadDto();
            dto.setUserId(msg.getUserId());
            dto.setRestaurantId(restaurantId);
            dto.setLastMessage(msg.getContent());
            dto.setLastMessageTime(msg.getTimestamp());
            
            userRepository.findById(msg.getUserId()).ifPresent(u -> {
                dto.setParticipantName(u.getName());
                dto.setParticipantImageUrl(u.getAvatarUrl());
            });
            return dto;
        }).collect(Collectors.toList());
    }
}
