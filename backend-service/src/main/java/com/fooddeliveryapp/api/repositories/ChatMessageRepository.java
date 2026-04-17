package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserIdAndRestaurantIdOrderByTimestampAsc(Long userId, Long restaurantId);

    @Query("SELECT m FROM ChatMessage m WHERE m.userId = :userId AND m.id IN (SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.userId = :userId GROUP BY m2.restaurantId) ORDER BY m.timestamp DESC")
    List<ChatMessage> findLatestMessagesForUser(@Param("userId") Long userId);

    @Query("SELECT m FROM ChatMessage m WHERE m.restaurantId = :restaurantId AND m.id IN (SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.restaurantId = :restaurantId GROUP BY m2.userId) ORDER BY m.timestamp DESC")
    List<ChatMessage> findLatestMessagesForRestaurant(@Param("restaurantId") Long restaurantId);
}
