package com.fooddeliveryapp.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private List<OrderItemRequest> items;
    private double deliveryFee;

    @Data
    public static class OrderItemRequest {
        private Long foodId;
        private int quantity;
    }
}
