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

        public long getFoodId() { return foodId; }
        public void setFoodId(long foodId) { this.foodId = foodId; }
        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }
        public String getFoodImageUrl() { return foodImageUrl; }
        public void setFoodImageUrl(String foodImageUrl) { this.foodImageUrl = foodImageUrl; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public double getLineTotal() { return lineTotal; }
        public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public List<OrderDetailItemDto> getItems() { return items; }
    public void setItems(List<OrderDetailItemDto> items) { this.items = items; }
}
