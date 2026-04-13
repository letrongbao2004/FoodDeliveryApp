package com.fooddeliveryapp.api.services;

import com.fooddeliveryapp.api.dto.OrderStatusResponse;
import com.fooddeliveryapp.api.dto.OrderStatusUpdateRequest;
import com.fooddeliveryapp.api.models.Order;
import com.fooddeliveryapp.api.models.OrderStatus;
import com.fooddeliveryapp.api.repositories.OrderRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class OrderTrackingService {

    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderTrackingService(OrderRepository orderRepository, SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public OrderStatusResponse updateOrderStatus(OrderStatusUpdateRequest request) {
        Long orderId = request.getOrderId();
        OrderStatus newStatus = request.getNewStatus();
        String role = request.getUpdatedBy();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        // 1. Role / Priority Validation
        validateStatusTransition(currentStatus, newStatus, role);

        // 2. Perform modification
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 3. Formulate Broadcast Response
        OrderStatusResponse response = new OrderStatusResponse(
                order.getId(),
                newStatus,
                order.getUpdatedAt(),
                role,
                "Order status updated to " + newStatus.name()
        );

        // 4. Send Event Over WebSockets
        messagingTemplate.convertAndSend("/topic/order/" + orderId, response);
        
        return response;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next, String role) {
        if ("SYSTEM".equalsIgnoreCase(role)) return; // System overrides anything

        // CUSTOMER can only trigger DELIVERED once it's OUT_FOR_DELIVERY
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            if (next == OrderStatus.DELIVERED) {
                if (current != OrderStatus.OUT_FOR_DELIVERY) {
                    throw new RuntimeException("Customer cannot mark ORDER DELIVERED unless it is OUT_FOR_DELIVERY");
                }
            } else if (next == OrderStatus.CANCELLED) {
                if (current != OrderStatus.ORDER_PLACED) {
                    throw new RuntimeException("Customer cannot CANCEL after processing started");
                }
            } else {
                throw new RuntimeException("Customer is not permitted to enforce status: " + next.name());
            }
        }

        // MERCHANT can trigger PACKED and OUT_FOR_DELIVERY
        if ("MERCHANT".equalsIgnoreCase(role)) {
            if (next == OrderStatus.ORDER_PACKED && current != OrderStatus.ORDER_PLACED) {
                throw new RuntimeException("Invalid transition to PACKED from " + current);
            }
            if (next == OrderStatus.OUT_FOR_DELIVERY && current != OrderStatus.ORDER_PACKED) {
                throw new RuntimeException("Invalid transition to OUT_FOR_DELIVERY from " + current);
            }
            if (next == OrderStatus.DELIVERED) {
                throw new RuntimeException("Merchant cannot mark DELIVERED. Waiting for Customer confirmation.");
            }
        }
    }
}
