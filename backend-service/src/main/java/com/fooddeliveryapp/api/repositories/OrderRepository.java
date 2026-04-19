package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_IdOrderByOrderDateDesc(Long userId);

    List<Order> findByRestaurant_IdOrderByOrderDateDesc(Long restaurantId);
}
