package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByRestaurantId(Long restaurantId);
}
