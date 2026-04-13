package com.fooddeliveryapp.api.dto;

import com.fooddeliveryapp.api.models.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private Long orderId;
    private OrderStatus newStatus;
    private String updatedBy; // "CUSTOMER" or "MERCHANT" or "SYSTEM"
}
