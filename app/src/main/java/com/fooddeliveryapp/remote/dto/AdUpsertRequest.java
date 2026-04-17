package com.fooddeliveryapp.remote.dto;

public class AdUpsertRequest {
    private String imageUrl;
    private String title;
    private String description;

    private long menuItemId;

    private String startDate;

    private String endDate;

    public AdUpsertRequest(String imageUrl, String title, String description, long menuItemId, String startDate, String endDate) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.menuItemId = menuItemId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
