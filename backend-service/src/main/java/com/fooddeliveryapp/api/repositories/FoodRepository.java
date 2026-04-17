package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByRestaurantId(Long restaurantId);

    boolean existsByIdAndRestaurantOwnerId(Long foodId, Long ownerId);

    @Query("select f from Food f join fetch f.restaurant where f.id = :id")
    Optional<Food> findByIdWithRestaurant(@Param("id") Long id);
}
