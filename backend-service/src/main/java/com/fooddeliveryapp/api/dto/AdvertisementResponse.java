package com.fooddeliveryapp.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fooddeliveryapp.api.models.Advertisement;
import com.fooddeliveryapp.api.models.Food;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdvertisementResponse {
    private Long id;

    @JsonProperty("image_url")
    private String imageUrl;

    private String title;
    private String description;

    @JsonProperty("menu_item")
    private MenuItemLite menuItem;

    public static AdvertisementResponse from(Advertisement advertisement) {
        Food food = advertisement.getMenuItem();
        return new AdvertisementResponse(
                advertisement.getId(),
                advertisement.getImageUrl(),
                advertisement.getTitle(),
                advertisement.getDescription(),
                new MenuItemLite(
                        food.getId(),
                        food.getName(),
                        food.getPrice(),
                        food.getRestaurant().getId()
                )
        );
    }

    @Data
    @AllArgsConstructor
    public static class MenuItemLite {
        private Long id;
        private String name;
        private double price;

        @JsonProperty("restaurant_id")
        private Long restaurantId;
    }
}
