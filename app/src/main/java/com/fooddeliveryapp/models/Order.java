package com.fooddeliveryapp.models;

import java.util.List;
import java.util.Date;
import com.fooddeliveryapp.utils.AppUtils;
import com.google.gson.annotations.SerializedName;

public class Order {
    // Must match backend OrderStatus enum / string values exactly
    public static final String STATUS_PENDING    = "PENDING";
    public static final String STATUS_CONFIRMED  = "CONFIRMED";
    public static final String STATUS_PREPARING  = "PREPARING";
    public static final String STATUS_DELIVERING = "DELIVERING";
    public static final String STATUS_DELIVERED  = "DELIVERED";
    public static final String STATUS_CANCELLED  = "CANCELLED";

    private long id;
    private int userId;
    private int restaurantId;
    private String restaurantName;
    private List<CartItem> items;
    private double subtotal;
    private double deliveryFee;
    private double discount;
    private double total;
    private String status;
    private String deliveryAddress;
    private String paymentMethod;
    private String voucherCode;
    private String notes;
    
    @SerializedName("orderDate")
    private Date orderDate;
    
    private long updatedAt;
    private String estimatedDelivery;

    public Order() {
    }

    // Getters & Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return orderDate != null ? orderDate.getTime() : 0;
    }

    public void setCreatedAt(long createdAt) {
        this.orderDate = new Date(createdAt);
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public String getOrderCode() {
        return "#FD" + String.format("%05d", id);
    }

    public String getTotalText() {
        return AppUtils.formatPrice(total);
    }
}