package com.fooddeliveryapp.api.dto;

import com.fooddeliveryapp.api.models.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderStatusResponse {
    private Long orderId;
    private OrderStatus status;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String message;

    public OrderStatusResponse() {}

    public OrderStatusResponse(Long orderId, OrderStatus status, LocalDateTime updatedAt, String updatedBy, String message) {
        this.orderId = orderId;
        this.status = status;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.message = message;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
