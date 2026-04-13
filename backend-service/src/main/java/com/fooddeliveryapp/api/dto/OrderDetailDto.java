package com.fooddeliveryapp.api.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class OrderDetailDto {
    private long id;
    private String orderCode;
    private String status;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private Date orderDate;
    private List<OrderDetailItemDto> items;

    @Data
    public static class OrderDetailItemDto {
        private long foodId;
        private String foodName;
        private String foodImageUrl;
        private int quantity;
        private double price;
        private double lineTotal;
    }
}
