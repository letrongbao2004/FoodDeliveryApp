package com.fooddeliveryapp.api.dto;

import com.fooddeliveryapp.api.models.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {
    private Long orderId;
    private OrderStatus status;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String message;
}
