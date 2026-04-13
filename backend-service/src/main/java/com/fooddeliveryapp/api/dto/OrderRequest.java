package com.fooddeliveryapp.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private Long restaurantId;
    private List<OrderItemRequest> items;
    private double deliveryFee;
    private String deliveryAddress;

    @Data
    public static class OrderItemRequest {
        private Long foodId;
        private int quantity;
    }
}
