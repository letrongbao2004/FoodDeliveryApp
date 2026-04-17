package com.fooddeliveryapp.models;

import com.google.gson.annotations.SerializedName;

public class Advertisement {
    private int id;

    @SerializedName("image_url")
    private String imageUrl;

    private String title;
    private String description;

    @SerializedName("menu_item")
    private MenuItem menuItem;

    public int getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public static class MenuItem {
        private int id;
        private String name;
        private double price;

        @SerializedName("restaurant_id")
        private int restaurantId;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getRestaurantId() {
            return restaurantId;
        }
    }
}
