package com.fooddeliveryapp.api.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddeliveryapp.api.dtos.OrderStatusUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrderWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Map orderId -> list of active sessions
    private final Map<Long, List<WebSocketSession>> orderSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New WebSocket connection: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("Received WS message: {}", payload);

        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root.has("action") && root.get("action").asText().equals("subscribe")) {
                if (root.has("orderId")) {
                    Long orderId = root.get("orderId").asLong();
                    orderSessions.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(session);
                    logger.info("Session {} subscribed to order ID: {}", session.getId(), orderId);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing websocket message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {}", session.getId());
        
        // Remove session from all tracked orders
        orderSessions.values().forEach(sessions -> sessions.remove(session));
        // Clean up empty lists
        orderSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public void broadcastToOrder(Long orderId, OrderStatusUpdateEvent event) {
        List<WebSocketSession> sessions = orderSessions.get(orderId);
        if (sessions != null && !sessions.isEmpty()) {
            try {
                String payload = objectMapper.writeValueAsString(event);
                TextMessage message = new TextMessage(payload);
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                }
                logger.info("Broadcasted update for order ID {} to {} connected clients", orderId, sessions.size());
            } catch (IOException e) {
                logger.error("Failed to broadcast to order ID: " + orderId, e);
            }
        } else {
            logger.info("No active WebSocket clients subscribed to order ID: {}", orderId);
        }
    }
}
