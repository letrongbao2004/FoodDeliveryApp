package com.fooddeliveryapp.api.dto;

import com.fooddeliveryapp.api.models.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private Long orderId;
    private OrderStatus newStatus;
    private String updatedBy; // "CUSTOMER" or "MERCHANT" or "SYSTEM"

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
