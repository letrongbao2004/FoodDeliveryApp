package com.fooddeliveryapp.api.services;

import com.fooddeliveryapp.api.dto.OrderDetailDto;

import com.fooddeliveryapp.api.dto.OrderRequest;
import com.fooddeliveryapp.api.models.Food;
import com.fooddeliveryapp.api.models.Order;
import com.fooddeliveryapp.api.models.OrderItem;
import com.fooddeliveryapp.api.models.User;
import com.fooddeliveryapp.api.models.Restaurant;
import com.fooddeliveryapp.api.repositories.FoodRepository;
import com.fooddeliveryapp.api.repositories.OrderRepository;
import com.fooddeliveryapp.api.repositories.RestaurantRepository;
import com.fooddeliveryapp.api.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, FoodRepository foodRepository, RestaurantRepository restaurantRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.restaurantRepository = restaurantRepository;
    }

    // @Transactional fixes partial updates and dangling logic
    @Transactional
    public Order placeOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        Restaurant restaurant = null;
        if (request.getRestaurantId() != null) {
            restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElse(null);
        }

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus("PENDING");
        order.setOrderDate(new Date());
        order.setDeliveryFee(request.getDeliveryFee());

        double subtotal = 0;

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            Food food = foodRepository.findById(itemReq.getFoodId())
                .orElseThrow(() -> new RuntimeException("Food not found"));
            
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setFood(food);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(food.getPrice());

            return item;
        }).collect(Collectors.toList());

        for (OrderItem item : items) {
            subtotal += item.getPrice() * item.getQuantity();
            // increment order count on the food item
            Food food = item.getFood();
            food.setOrderCount(food.getOrderCount() + item.getQuantity());
            foodRepository.save(food);
        }

        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal + order.getDeliveryFee());

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setOrderCode("#FD" + String.format("%05d", order.getId()));
        dto.setStatus(order.getStatus());
        dto.setSubtotal(order.getSubtotal());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setTotal(order.getTotal());
        dto.setOrderDate(order.getOrderDate());

        List<OrderDetailDto.OrderDetailItemDto> itemDtos = order.getItems().stream().map(item -> {
            OrderDetailDto.OrderDetailItemDto i = new OrderDetailDto.OrderDetailItemDto();
            i.setFoodId(item.getFood().getId());
            i.setFoodName(item.getFood().getName());
            i.setFoodImageUrl(item.getFood().getImageUrl());
            i.setQuantity(item.getQuantity());
            i.setPrice(item.getPrice());
            i.setLineTotal(item.getPrice() * item.getQuantity());
            return i;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}
