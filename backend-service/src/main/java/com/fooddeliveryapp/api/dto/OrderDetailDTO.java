package com.fooddeliveryapp.api.dto;

import com.fooddeliveryapp.api.models.Order;
import com.fooddeliveryapp.api.models.OrderItem;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDetailDTO {

    private Long id;
    private String orderCode;
    private String status;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private Date orderDate;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long foodId;
        private String foodName;
        private String foodImageUrl;
        private int quantity;
        private double price;
        private double lineTotal;
    }

    public static OrderDetailDTO from(Order order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderCode("#FD" + String.format("%05d", order.getId()));
        dto.setStatus(order.getStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setTotal(order.getTotal());
        dto.setOrderDate(order.getOrderDate());

        if (order.getItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {
                OrderItemDTO i = new OrderItemDTO();
                i.setFoodId(item.getFood().getId());
                i.setFoodName(item.getFood().getName());
                i.setFoodImageUrl(item.getFood().getImageUrl());
                i.setQuantity(item.getQuantity());
                i.setPrice(item.getPrice());
                i.setLineTotal(item.getPrice() * item.getQuantity());
                return i;
            }).collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }
}
