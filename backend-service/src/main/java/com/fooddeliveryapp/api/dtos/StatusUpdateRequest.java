package com.fooddeliveryapp.api.dtos;

import com.fooddeliveryapp.api.models.OrderStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private OrderStatus status;
    private String updatedBy;
}
