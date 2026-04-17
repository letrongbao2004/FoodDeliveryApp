package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.dto.AdvertisementResponse;
import com.fooddeliveryapp.api.dto.AdvertisementUpsertRequest;
import com.fooddeliveryapp.api.models.Advertisement;
import com.fooddeliveryapp.api.models.Food;
import com.fooddeliveryapp.api.models.Role;
import com.fooddeliveryapp.api.repositories.AdvertisementRepository;
import com.fooddeliveryapp.api.repositories.FoodRepository;
import com.fooddeliveryapp.api.security.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdvertisementController {
    private final AdvertisementRepository advertisementRepository;
    private final FoodRepository foodRepository;

    public AdvertisementController(AdvertisementRepository advertisementRepository, FoodRepository foodRepository) {
        this.advertisementRepository = advertisementRepository;
        this.foodRepository = foodRepository;
    }

    @GetMapping
    public ResponseEntity<List<AdvertisementResponse>> getActiveAds(@RequestParam(defaultValue = "5") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 10));
        List<AdvertisementResponse> data = advertisementRepository
                .findActiveAds(LocalDateTime.now(), PageRequest.of(0, safeLimit))
                .stream()
                .map(AdvertisementResponse::from)
                .toList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMyAds(Authentication authentication) {
        JwtPrincipal principal = getMerchantPrincipal(authentication);
        if (principal == null) {
            return ResponseEntity.status(403).body("Merchant access required");
        }
        List<AdvertisementResponse> data = advertisementRepository.findByMerchantIdWithMenuItem(principal.userId())
                .stream()
                .map(AdvertisementResponse::from)
                .toList();
        return ResponseEntity.ok(data);
    }

    @PostMapping
    public ResponseEntity<?> createAd(@Valid @RequestBody AdvertisementUpsertRequest request,
                                      Authentication authentication) {
        JwtPrincipal principal = getMerchantPrincipal(authentication);
        if (principal == null) {
            return ResponseEntity.status(403).body("Merchant access required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            return ResponseEntity.badRequest().body("end_date must be after start_date");
        }
        if (!foodRepository.existsByIdAndRestaurantOwnerId(request.getMenuItemId(), principal.userId())) {
            return ResponseEntity.status(403).body("You can only create ads for your own menu items");
        }

        Food food = foodRepository.findByIdWithRestaurant(request.getMenuItemId())
                .orElse(null);
        if (food == null) {
            return ResponseEntity.badRequest().body("menu_item_id not found");
        }

        Advertisement ad = new Advertisement();
        ad.setImageUrl(request.getImageUrl());
        ad.setTitle(request.getTitle());
        ad.setDescription(request.getDescription());
        ad.setMenuItem(food);
        ad.setMerchantId(principal.userId());
        ad.setStartDate(request.getStartDate());
        ad.setEndDate(request.getEndDate());
        ad.setActive(true);

        Advertisement saved = advertisementRepository.save(ad);
        return ResponseEntity.ok(AdvertisementResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAd(@PathVariable Long id,
                                      @Valid @RequestBody AdvertisementUpsertRequest request,
                                      Authentication authentication) {
        JwtPrincipal principal = getMerchantPrincipal(authentication);
        if (principal == null) {
            return ResponseEntity.status(403).body("Merchant access required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            return ResponseEntity.badRequest().body("end_date must be after start_date");
        }

        Advertisement ad = advertisementRepository.findByIdWithMenuItem(id).orElse(null);
        if (ad == null) {
            return ResponseEntity.notFound().build();
        }
        if (!ad.getMerchantId().equals(principal.userId())) {
            return ResponseEntity.status(403).body("You can only update your own ads");
        }
        if (!foodRepository.existsByIdAndRestaurantOwnerId(request.getMenuItemId(), principal.userId())) {
            return ResponseEntity.status(403).body("You can only use your own menu items");
        }

        Food food = foodRepository.findByIdWithRestaurant(request.getMenuItemId()).orElse(null);
        if (food == null) {
            return ResponseEntity.badRequest().body("menu_item_id not found");
        }

        ad.setImageUrl(request.getImageUrl());
        ad.setTitle(request.getTitle());
        ad.setDescription(request.getDescription());
        ad.setMenuItem(food);
        ad.setStartDate(request.getStartDate());
        ad.setEndDate(request.getEndDate());

        return ResponseEntity.ok(AdvertisementResponse.from(advertisementRepository.save(ad)));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disableAd(@PathVariable Long id, Authentication authentication) {
        JwtPrincipal principal = getMerchantPrincipal(authentication);
        if (principal == null) {
            return ResponseEntity.status(403).body("Merchant access required");
        }
        Advertisement ad = advertisementRepository.findById(id).orElse(null);
        if (ad == null) {
            return ResponseEntity.notFound().build();
        }
        if (!ad.getMerchantId().equals(principal.userId())) {
            return ResponseEntity.status(403).body("You can only disable your own ads");
        }
        ad.setActive(false);
        return ResponseEntity.ok(AdvertisementResponse.from(advertisementRepository.save(ad)));
    }

    private JwtPrincipal getMerchantPrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            return null;
        }
        if (principal.role() != Role.MERCHANT) {
            return null;
        }
        return principal;
    }
}
