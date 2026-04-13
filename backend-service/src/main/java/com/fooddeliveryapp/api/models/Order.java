package com.fooddeliveryapp.api.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    @JsonIgnore
    private Restaurant restaurant;

    // Expose userId directly for the Android client
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public Long getRestaurantId() {
        return restaurant != null ? restaurant.getId() : null;
    }

    public String getRestaurantName() {
        return restaurant != null ? restaurant.getName() : null;
    }

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, DELIVERING, COMPLETED, CANCELLED

    private double subtotal;
    private double deliveryFee;
    private double total;

    private String deliveryAddress;

    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;

    // Fixes the Optimistic Locking Exception!
    @Version
    private Long version;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private List<OrderItem> items;
}
