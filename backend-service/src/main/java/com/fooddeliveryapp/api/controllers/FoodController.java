package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.models.Food;
import com.fooddeliveryapp.api.models.Restaurant;
import com.fooddeliveryapp.api.models.Role;
import com.fooddeliveryapp.api.repositories.FoodRepository;
import com.fooddeliveryapp.api.repositories.RestaurantRepository;
import com.fooddeliveryapp.api.security.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public ResponseEntity<?> addFood(Authentication authentication, @Valid @RequestBody Food food) {
        JwtPrincipal principal = getPrincipal(authentication);
        
        if (food.getRestaurant() == null || food.getRestaurant().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "restaurant.id is required"));
        }

        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(food.getRestaurant().getId());
        if (restaurantOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nhà hàng không tồn tại"));
        }

        Restaurant restaurant = restaurantOpt.get();
        if (principal.role() != Role.ADMIN && !principal.userId().equals(restaurant.getOwnerId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền thêm món cho nhà hàng này"));
        }

        food.setRestaurant(restaurant);
        return ResponseEntity.ok(foodRepository.save(food));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFood(Authentication authentication, @PathVariable Long id, @Valid @RequestBody Food updated) {
        JwtPrincipal principal = getPrincipal(authentication);
        
        return foodRepository.findById(id).map(food -> {
            Restaurant restaurant = food.getRestaurant();
            if (principal.role() != Role.ADMIN && (restaurant == null || !principal.userId().equals(restaurant.getOwnerId()))) {
                return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền chỉnh sửa món ăn này"));
            }

            food.setName(updated.getName());
            food.setDescription(updated.getDescription());
            food.setPrice(updated.getPrice());
            food.setImageUrl(updated.getImageUrl());
            food.setImagePublicId(updated.getImagePublicId());
            food.setCategory(updated.getCategory());
            food.setAvailable(updated.isAvailable());
            food.setBestSeller(updated.isBestSeller());
            food.setNew(updated.isNew());
            food.setRating(updated.getRating());
            
            return ResponseEntity.ok(foodRepository.save(food));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFood(Authentication authentication, @PathVariable Long id) {
        JwtPrincipal principal = getPrincipal(authentication);
        
        Optional<Food> foodOpt = foodRepository.findById(id);
        if (foodOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy món ăn"));
        }

        Food food = foodOpt.get();
        Restaurant restaurant = food.getRestaurant();
        if (principal.role() != Role.ADMIN && (restaurant == null || !principal.userId().equals(restaurant.getOwnerId()))) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xóa món ăn này"));
        }

        foodRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private JwtPrincipal getPrincipal(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal p) {
            return p;
        }
        throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized"
        );
    }
}
