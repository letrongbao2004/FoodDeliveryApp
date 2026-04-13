package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.models.Restaurant;
import com.fooddeliveryapp.api.repositories.RestaurantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    public RestaurantController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        // JOIN FETCH businessHours để isOpen() tính đúng
        return ResponseEntity.ok(restaurantRepository.findAllWithHours());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRestaurant(@PathVariable Long id) {
        // JOIN FETCH businessHours để isOpen() tính đúng
        return restaurantRepository.findByIdWithHours(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getRestaurantByOwner(@PathVariable Long ownerId) {
        return restaurantRepository.findByOwnerIdWithHours(ownerId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Restaurant> addRestaurant(@Valid @RequestBody Restaurant restaurant) {
        return ResponseEntity.ok(restaurantRepository.save(restaurant));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Long id, @Valid @RequestBody Restaurant updated) {
        return restaurantRepository.findById(id).map(r -> {
            r.setName(updated.getName());
            r.setDescription(updated.getDescription());
            r.setImageUrl(updated.getImageUrl());
            r.setImagePublicId(updated.getImagePublicId());
            r.setAddress(updated.getAddress());
            r.setPhone(updated.getPhone());
            r.setCategory(updated.getCategory());
            r.setDeliveryFee(updated.getDeliveryFee());
            r.setDeliveryTime(updated.getDeliveryTime());
            return ResponseEntity.ok(restaurantRepository.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }
}
