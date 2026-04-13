package com.fooddeliveryapp.api.controllers;

import com.fooddeliveryapp.api.services.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5MB

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(value = "/restaurant-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadRestaurantLogo(@RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "file is required"));
        }
        if (file.getSize() > MAX_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("error", "file too large (max 5MB)"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "only image files are allowed"));
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot).toLowerCase();
        }

        String filename = "restaurant-logo-" + UUID.randomUUID() + ext;
        CloudinaryService.UploadResult uploaded = cloudinaryService.uploadImage(file, "restaurant-logo");
        // Keep old response shape for existing Android flow
        return ResponseEntity.ok(Map.of("url", uploaded.imageUrl(), "filename", filename));
    }
}

