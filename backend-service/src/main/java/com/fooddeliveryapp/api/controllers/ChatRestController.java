package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.dto.ChatThreadDto;
import com.fooddeliveryapp.api.models.ChatMessage;
import com.fooddeliveryapp.api.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(@RequestParam Long userId, @RequestParam Long restaurantId) {
        return ResponseEntity.ok(chatService.getHistory(userId, restaurantId));
    }

    @GetMapping("/threads/user/{userId}")
    public ResponseEntity<List<ChatThreadDto>> getUserThreads(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getUserThreads(userId));
    }

    @GetMapping("/threads/restaurant/{restaurantId}")
    public ResponseEntity<List<ChatThreadDto>> getRestaurantThreads(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(chatService.getRestaurantThreads(restaurantId));
    }
}
