package com.fooddeliveryapp.api.config;

import com.fooddeliveryapp.api.models.*;
import com.fooddeliveryapp.api.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo,
                                   RestaurantRepository restaurantRepo,
                                   FoodRepository foodRepo,
                                   OrderRepository orderRepo) {
        return args -> {
            // Seed User
            User testUser;
            if (userRepo.count() == 0) {
                testUser = new User(null, "customer@test.com", "password", "Test Customer", "0123456789", "123 Test St", "CUSTOMER", 0L);
                testUser = userRepo.save(testUser);
            } else {
                testUser = userRepo.findAll().get(0);
            }

            // Seed Restaurant
            Restaurant testRestaurant;
            if (restaurantRepo.count() == 0) {
                testRestaurant = new Restaurant(null, "KFC", "Famous Fried Chicken", 
                        "https://images.unsplash.com/photo-1513104890138-7c749659a591", 
                        4.5, 30, 15000.0, 0L, null);
                testRestaurant = restaurantRepo.save(testRestaurant);
            } else {
                testRestaurant = restaurantRepo.findAll().get(0);
            }

            // Seed Food
            Food burger;
            Food fries;
            if (foodRepo.count() == 0) {
                burger = new Food(null, testRestaurant, "Zinger Burger", "Crispy chicken burger", 55000, 
                        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd", "Fast Food", true, true, true, 4.8, 500, 0L);
                fries = new Food(null, testRestaurant, "French Fries", "Golden crispy fries", 25000, 
                        "https://images.unsplash.com/photo-1576107232684-1279f390859f", "Fast Food", true, false, false, 4.2, 300, 0L);
                burger = foodRepo.save(burger);
                fries = foodRepo.save(fries);
            } else {
                burger = foodRepo.findAll().get(0);
                fries = foodRepo.findAll().size() > 1 ? foodRepo.findAll().get(1) : burger;
            }

            // Seed an Order to test Order Detail
            if (orderRepo.count() == 0) {
                Order order = new Order();
                order.setUser(testUser);
                order.setStatus("SUCCESS");
                order.setSubtotal(80000);
                order.setDeliveryFee(15000);
                order.setTotal(95000);
                order.setOrderDate(new Date());

                OrderItem item1 = new OrderItem(null, order, burger, 1, 55000);
                OrderItem item2 = new OrderItem(null, order, fries, 1, 25000);

                order.setItems(List.of(item1, item2));

                orderRepo.save(order);
                System.out.println("============== 🚀 DUMMY MOCK DATA SEEDED 🚀 ==============");
                System.out.println("Test Customer inserted.");
                System.out.println("Dummy Order #1 inserted for Order Details testing.");
            }
        };
    }
}
