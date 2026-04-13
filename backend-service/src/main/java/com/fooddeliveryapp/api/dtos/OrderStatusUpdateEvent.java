package com.fooddeliveryapp.api.dtos;

import com.fooddeliveryapp.api.models.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateEvent {
    private Long orderId;
    private OrderStatus status;
    private String updatedBy;
    private Date timestamp;
}
