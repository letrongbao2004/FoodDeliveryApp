package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.models.Food;
import com.fooddeliveryapp.api.repositories.FoodRepository;
import com.fooddeliveryapp.api.repositories.RestaurantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;

    public FoodController(FoodRepository foodRepository, RestaurantRepository restaurantRepository) {
        this.foodRepository = foodRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public ResponseEntity<List<Food>> getFoods(@RequestParam(required = false) Long restaurantId) {
        if (restaurantId != null) {
            return ResponseEntity.ok(foodRepository.findByRestaurantId(restaurantId));
        }
        return ResponseEntity.ok(foodRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFood(@PathVariable Long id) {
        return foodRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> addFood(@RequestBody Food food) {
        if (food.getRestaurant() == null || food.getRestaurant().getId() == null) {
            return ResponseEntity.badRequest().body("restaurant.id is required");
        }
        // Attach managed Restaurant entity to avoid Detached entity error
        food.setRestaurant(restaurantRepository.getReferenceById(food.getRestaurant().getId()));
        return ResponseEntity.ok(foodRepository.save(food));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFood(@PathVariable Long id, @RequestBody Food updated) {
        return foodRepository.findById(id).map(food -> {
            food.setName(updated.getName());
            food.setDescription(updated.getDescription());
            food.setPrice(updated.getPrice());
            food.setImageUrl(updated.getImageUrl());
            food.setCategory(updated.getCategory());
            food.setAvailable(updated.isAvailable());
            food.setBestSeller(updated.isBestSeller());
            food.setNew(updated.isNew());
            food.setRating(updated.getRating());
            return ResponseEntity.ok(foodRepository.save(food));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFood(@PathVariable Long id) {
        if (!foodRepository.existsById(id)) return ResponseEntity.notFound().build();
        foodRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
