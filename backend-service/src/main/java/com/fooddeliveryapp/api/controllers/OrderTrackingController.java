package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.dto.OrderStatusResponse;
import com.fooddeliveryapp.api.dto.OrderStatusUpdateRequest;
import com.fooddeliveryapp.api.repositories.OrderRepository;
import com.fooddeliveryapp.api.services.OrderTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
public class OrderTrackingController {

    private final OrderTrackingService orderTrackingService;
    private final OrderRepository orderRepository;

    public OrderTrackingController(OrderTrackingService orderTrackingService, OrderRepository orderRepository) {
        this.orderTrackingService = orderTrackingService;
        this.orderRepository = orderRepository;
    }

    // Client Android calls STOMP send to /app/order/update-status
    @MessageMapping("/order/update-status")
    public void handleOrderStatusUpdate(OrderStatusUpdateRequest request) {
        try {
            orderTrackingService.updateOrderStatus(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<OrderStatusResponse> getCurrentStatus(@PathVariable Long id) {
        return orderRepository.findById(id).map(order -> {
            OrderStatusResponse response = new OrderStatusResponse();
            response.setOrderId(order.getId());
            response.setStatus(order.getStatus());
            response.setUpdatedAt(order.getUpdatedAt());
            response.setMessage("Synced explicit status over REST");
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }
}
