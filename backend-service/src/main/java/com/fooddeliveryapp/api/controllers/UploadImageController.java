package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.dto.UploadContext;
import com.fooddeliveryapp.api.dto.UploadImageResponse;
import com.fooddeliveryapp.api.models.Food;
import com.fooddeliveryapp.api.models.Restaurant;
import com.fooddeliveryapp.api.models.Role;
import com.fooddeliveryapp.api.models.User;
import com.fooddeliveryapp.api.repositories.FoodRepository;
import com.fooddeliveryapp.api.repositories.RestaurantRepository;
import com.fooddeliveryapp.api.repositories.UserRepository;
import com.fooddeliveryapp.api.security.JwtPrincipal;
import com.fooddeliveryapp.api.services.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/api/upload")
public class UploadImageController {

    private static final long MAX_BYTES = 15L * 1024 * 1024; // 15MB

    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;

    public UploadImageController(
            CloudinaryService cloudinaryService,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            FoodRepository foodRepository
    ) {
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.foodRepository = foodRepository;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            Authentication authentication,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "context", required = false) String contextRaw,
            @RequestPart(value = "entity_id", required = false) String entityIdRaw
    ) throws Exception {
        JwtPrincipal principal = requirePrincipal(authentication);

        if (!StringUtils.hasText(contextRaw)) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "context is required"));
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "file is required"));
        }
        if (file.getSize() > MAX_BYTES) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "file too large (max 15MB)"));
        }
        String contentType = file.getContentType();
        if (contentType == null) contentType = "";
        String ct = contentType.toLowerCase(Locale.ROOT);
        // Android clients sometimes send "image/*" for multipart uploads.
        // Accept any image/* and do a lightweight allowlist for common formats.
        if (!(ct.startsWith("image/") || ct.equals("application/octet-stream"))) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "only image files are allowed"));
        }
        // Extra safety: if a concrete image type is provided, allow only common formats
        if (ct.startsWith("image/") && !ct.equals("image/*")) {
            if (!(ct.equals("image/jpeg") || ct.equals("image/jpg") || ct.equals("image/png") || ct.equals("image/webp"))) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "invalid image type (jpg/png/webp only)"));
            }
        }

        UploadContext ctx;
        try {
            ctx = UploadContext.valueOf(contextRaw);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "invalid context"));
        }

        Long entityId = null;
        if (StringUtils.hasText(entityIdRaw)) {
            try {
                entityId = Long.parseLong(entityIdRaw.trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "entity_id must be a number"));
            }
        }

        // Role/context validation
        if (ctx == UploadContext.avatar) {
            // any logged-in user ok
            return ResponseEntity.ok(handleAvatar(principal, file));
        }

        // restaurant/menu_item => merchant only
        if (principal.role() != Role.MERCHANT && principal.role() != Role.ADMIN) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "merchant role required"));
        }

        if (ctx == UploadContext.restaurant) {
            return ResponseEntity.ok(handleRestaurant(principal, file, entityId));
        }
        if (ctx == UploadContext.menu_item) {
            return ResponseEntity.ok(handleMenuItem(principal, file, entityId));
        }

        return ResponseEntity.badRequest().body(java.util.Map.of("error", "invalid context"));
    }

    private UploadImageResponse handleAvatar(JwtPrincipal principal, MultipartFile file) throws Exception {
        User user = userRepository.findById(principal.userId()).orElseThrow();

        CloudinaryService.UploadResult uploaded = cloudinaryService.uploadImage(
                file,
                "avatar/user-" + principal.userId()
        );

        String oldPublicId = user.getAvatarPublicId();
        user.setAvatarUrl(uploaded.imageUrl());
        user.setAvatarPublicId(uploaded.publicId());
        userRepository.save(user);

        // best-effort cleanup
        if (oldPublicId != null && !oldPublicId.equals(uploaded.publicId())) {
            cloudinaryService.deleteByPublicId(oldPublicId);
        }

        return new UploadImageResponse(uploaded.imageUrl(), uploaded.publicId());
    }

    private UploadImageResponse handleRestaurant(JwtPrincipal principal, MultipartFile file, Long restaurantId) throws Exception {
        if (restaurantId != null && restaurantId > 0) {
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
            if (!principal.userId().equals(restaurant.getOwnerId()) && principal.role() != Role.ADMIN) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "not owner of restaurant"
                );
            }

            CloudinaryService.UploadResult uploaded = cloudinaryService.uploadImage(
                    file,
                    "restaurant/" + restaurantId
            );

            String oldPublicId = restaurant.getImagePublicId();
            restaurant.setImageUrl(uploaded.imageUrl());
            restaurant.setImagePublicId(uploaded.publicId());
            restaurantRepository.save(restaurant);

            if (oldPublicId != null && !oldPublicId.equals(uploaded.publicId())) {
                cloudinaryService.deleteByPublicId(oldPublicId);
            }

            return new UploadImageResponse(uploaded.imageUrl(), uploaded.publicId());
        }

        // Pre-upload mode (create flow): just upload and return URL/public_id
        CloudinaryService.UploadResult uploaded = cloudinaryService.uploadImage(
                file,
                "restaurant/user-" + principal.userId()
        );
        return new UploadImageResponse(uploaded.imageUrl(), uploaded.publicId());
    }

    private UploadImageResponse handleMenuItem(JwtPrincipal principal, MultipartFile file, Long foodId) throws Exception {
        if (foodId != null && foodId > 0) {
            Food food = foodRepository.findById(foodId).orElseThrow();
            Long ownerId = Optional.ofNullable(food.getRestaurant()).map(Restaurant::getOwnerId).orElse(null);
            if (ownerId == null || (!principal.userId().equals(ownerId) && principal.role() != Role.ADMIN)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "not owner of menu item"
                );
            }

            CloudinaryService.UploadResult uploaded = cloudinaryService.uploadImage(
                    file,
                    "menu-item/" + foodId
            );

            String oldPublicId = food.getImagePublicId();
            food.setImageUrl(uploaded.imageUrl());
            food.setImagePublicId(uploaded.publicId());
            foodRepository.save(food);

            if (oldPublicId != null && !oldPublicId.equals(uploaded.publicId())) {
                cloudinaryService.deleteByPublicId(oldPublicId);
            }

            return new UploadImageResponse(uploaded.imageUrl(), uploaded.publicId());
        }

        CloudinaryService.UploadResult uploaded = cloudinaryService.uploadImage(
                file,
                "menu-item/user-" + principal.userId()
        );
        return new UploadImageResponse(uploaded.imageUrl(), uploaded.publicId());
    }

    private JwtPrincipal requirePrincipal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal p)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Unauthorized"
            );
        }
        return p;
    }
}

