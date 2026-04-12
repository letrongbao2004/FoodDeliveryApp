package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.food.restaurant.id = :restaurantId")
    List<Order> findByRestaurantId(@org.springframework.data.repository.query.Param("restaurantId") Long restaurantId);
}
