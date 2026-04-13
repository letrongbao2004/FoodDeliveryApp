package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Fetch all restaurants kèm businessHours trong một câu query duy nhất.
     * Dùng DISTINCT để tránh duplicate do LEFT JOIN.
     */
    @Query("SELECT DISTINCT r FROM Restaurant r LEFT JOIN FETCH r.businessHours")
    List<Restaurant> findAllWithHours();

    /**
     * Fetch một restaurant kèm businessHours.
     */
    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.businessHours WHERE r.id = :id")
    Optional<Restaurant> findByIdWithHours(Long id);
}
