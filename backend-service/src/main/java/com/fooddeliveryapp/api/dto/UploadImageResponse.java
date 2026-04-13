package com.fooddeliveryapp.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UploadImageResponse(
        @JsonProperty("image_url") String imageUrl,
        @JsonProperty("public_id") String publicId
) {
}

