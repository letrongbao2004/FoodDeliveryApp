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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;
    private final ChatService chatService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        FoodRepository foodRepository, RestaurantRepository restaurantRepository,
                        ChatService chatService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.restaurantRepository = restaurantRepository;
        this.chatService = chatService;
    }

    // @Transactional fixes partial updates and dangling logic
    @Transactional
    public Order placeOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        Restaurant restaurant = null;
        if (request.getRestaurantId() != null && request.getRestaurantId() > 0) {
            restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElse(null);
        }

        // --- BUG FIX CHÍNH: Nếu Android gửi thiếu/sai restaurantId (=0), ta tự tìm lại thông qua Món ăn đầu tiên
        if (restaurant == null && request.getItems() != null && !request.getItems().isEmpty()) {
            Long firstFoodId = request.getItems().get(0).getFoodId();
            Food firstFood = foodRepository.findById(firstFoodId).orElse(null);
            if (firstFood != null) {
                restaurant = firstFood.getRestaurant();
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(com.fooddeliveryapp.api.models.OrderStatus.ORDER_PLACED);
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

        Order saved = orderRepository.save(order);

        // Send system message to chat after order is saved
        if (restaurant != null) {
            try {
                chatService.sendOrderConfirmation(user.getId(), restaurant.getId(), saved.getId());
            } catch (Exception e) {
                log.warn("Could not send order-confirmation chat message: {}", e.getMessage());
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setOrderCode("#FD" + String.format("%05d", order.getId()));
        dto.setStatus(order.getStatus().name());
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
