package com.fooddeliveryapp.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdvertisementUpsertRequest {
    @NotBlank
    private String imageUrl;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long menuItemId;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;
}
