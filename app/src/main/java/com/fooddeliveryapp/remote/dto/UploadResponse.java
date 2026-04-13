package com.fooddeliveryapp.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    /**
     * Backward compatible: some endpoints return { "url": "..." }
     */
    private String url;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("public_id")
    private String publicId;

    public String getUrl() {
        // Prefer new contract if present, fall back to legacy
        return (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
}

