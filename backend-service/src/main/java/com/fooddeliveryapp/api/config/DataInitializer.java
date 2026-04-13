package com.fooddeliveryapp.api.config;

import com.fooddeliveryapp.api.models.*;
import com.fooddeliveryapp.api.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;
    private final OrderRepository orderRepository;
    private final BusinessHoursRepository businessHoursRepository;

    public DataInitializer(UserRepository userRepository,
                           RestaurantRepository restaurantRepository,
                           FoodRepository foodRepository,
                           OrderRepository orderRepository,
                           BusinessHoursRepository businessHoursRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.foodRepository = foodRepository;
        this.orderRepository = orderRepository;
        this.businessHoursRepository = businessHoursRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("Initializing database with sample data...");

        // 1. Users
        User admin = new User(null, "admin@food.com", "password", "Admin User", "0123456789", "123 Admin St", "ADMIN", 0L);
        User owner = new User(null, "owner@food.com", "password", "Restaurant Owner", "0987654321", "456 Owner Rd", "OWNER", 0L);
        User customer = new User(null, "customer@food.com", "password", "John Doe", "0555123456", "789 Customer Ave", "CUSTOMER", 0L);
        
        userRepository.saveAll(Arrays.asList(admin, owner, customer));

        // 2. Restaurants
        Restaurant r1 = new Restaurant(null, "Pizza Palace", "Authentic Italian Pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591", 4.5, 30, 2.5, 0L, null);
        Restaurant r2 = new Restaurant(null, "Burger House", "Juicy American Burgers", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd", 4.2, 25, 1.5, 0L, null);
        Restaurant r3 = new Restaurant(null, "Sushi Zen", "Fresh Japanese Sushi", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c", 4.8, 40, 3.0, 0L, null);

        restaurantRepository.saveAll(Arrays.asList(r1, r2, r3));

        // 3. Business Hours (3 rows total as requested)
        BusinessHours bh1 = new BusinessHours(null, r1, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(22, 0), false);
        BusinessHours bh2 = new BusinessHours(null, r2, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(21, 0), false);
        BusinessHours bh3 = new BusinessHours(null, r3, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(23, 0), false);
        
        businessHoursRepository.saveAll(Arrays.asList(bh1, bh2, bh3));

        // 4. Foods (3 rows total)
        Food f1 = new Food(null, r1, "Margherita Pizza", "Classic tomato and mozzarella", 12.99, "https://images.unsplash.com/photo-1604068549290-dea0e4a305ca", "Pizza", true, true, false, 4.5, 100, 0L);
        Food f2 = new Food(null, r2, "Cheeseburger", "Beef patty with cheddar cheese", 8.50, "https://images.unsplash.com/photo-1572802419224-296b0aeee0d9", "Burger", true, false, true, 4.0, 50, 0L);
        Food f3 = new Food(null, r3, "Salmon Nigiri", "Fresh salmon over seasoned rice", 15.00, "https://images.unsplash.com/photo-1583623025817-d180a2221d0a", "Sushi", true, true, true, 4.9, 80, 0L);

        foodRepository.saveAll(Arrays.asList(f1, f2, f3));

        // 5. Orders (3 rows total)
        Order o1 = new Order(null, customer, "COMPLETED", 12.99, 2.5, 15.49, new Date(), 0L, new ArrayList<>());
        Order o2 = new Order(null, customer, "PENDING", 8.50, 1.5, 10.00, new Date(), 0L, new ArrayList<>());
        Order o3 = new Order(null, owner, "PROCESSING", 15.00, 3.0, 18.00, new Date(), 0L, new ArrayList<>());

        orderRepository.saveAll(Arrays.asList(o1, o2, o3));

        // 6. OrderItems (3 rows total)
        OrderItem oi1 = new OrderItem(null, o1, f1, 1, 12.99);
        OrderItem oi2 = new OrderItem(null, o2, f2, 1, 8.50);
        OrderItem oi3 = new OrderItem(null, o3, f3, 1, 15.00);

        // Since Order has CascadeType.ALL, we can just save them via order or manually.
        // But the user asked for each table. There's no OrderItemRepository, 
        // and usually OrderItems are saved via Order.
        o1.getItems().add(oi1);
        o2.getItems().add(oi2);
        o3.getItems().add(oi3);

        orderRepository.saveAll(Arrays.asList(o1, o2, o3));

        System.out.println("Database initialization completed.");
    }
}
