package com.fooddeliveryapp.api.repositories;

import com.fooddeliveryapp.api.models.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Long> {

    List<BusinessHours> findByRestaurantId(Long restaurantId);

    Optional<BusinessHours> findByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);
}
