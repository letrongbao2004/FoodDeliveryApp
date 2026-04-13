package com.fooddeliveryapp.models;

import java.util.Date;
import java.util.List;

public class OrderDetail {
    private long id;
    private String orderCode;
    private OrderStatus status;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private Date orderDate;
    private List<OrderDetailItem> items;

    public static class OrderDetailItem {
        private long foodId;
        private String foodName;
        private String foodImageUrl;
        private int quantity;
        private double price;
        private double lineTotal;

        public long getFoodId() {
            return foodId;
        }

        public String getFoodName() {
            return foodName;
        }

        public String getFoodImageUrl() {
            return foodImageUrl;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public double getLineTotal() {
            return lineTotal;
        }
    }

    public long getId() {
        return id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public double getTotal() {
        return total;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public List<OrderDetailItem> getItems() {
        return items;
    }
}