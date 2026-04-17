package com.fooddeliveryapp.api.services;

import com.fooddeliveryapp.api.dto.*;
import com.fooddeliveryapp.api.models.*;
import com.fooddeliveryapp.api.repositories.*;
import com.fooddeliveryapp.api.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        FoodRepository foodRepository, RestaurantRepository restaurantRepository,
                        ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.restaurantRepository = restaurantRepository;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Order placeOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        Restaurant restaurant = null;
        if (request.getRestaurantId() != null && request.getRestaurantId() > 0) {
            restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElse(null);
        }

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
        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setOrderDate(new Date());
        order.setDeliveryFee(request.getDeliveryFee());

        double subtotal = 0;
        List<OrderItem> items = new ArrayList<>();

        Set<Long> foodIds = request.getItems().stream()
                .map(OrderRequest.OrderItemRequest::getFoodId)
                .collect(Collectors.toSet());
        
        Map<Long, Food> foodMap = foodRepository.findAllById(foodIds).stream()
                .collect(Collectors.toMap(Food::getId, Function.identity()));

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Food food = foodMap.get(itemReq.getFoodId());
            if (food == null) {
                throw new ResourceNotFoundException("Food not found: " + itemReq.getFoodId());
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setFood(food);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(food.getPrice());
            items.add(item);

            subtotal += item.getPrice() * item.getQuantity();
            food.setOrderCount(food.getOrderCount() + item.getQuantity());
        }
        
        foodRepository.saveAll(foodMap.values());
        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal + order.getDeliveryFee());

        Order saved = orderRepository.save(order);

        try {
            chatService.sendOrderConfirmation(user.getId(), restaurant != null ? restaurant.getId() : 0L, saved.getId());
        } catch (Exception e) {
            log.error("Failed to send system chat message for order {}: {}", saved.getId(), e.getMessage());
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

    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        
        order.setStatus(status);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // Broadcast update via WebSocket
        try {
            Map<String, Object> payload = Map.of(
                "orderId", saved.getId(),
                "status", status.name(),
                "updatedAt", saved.getUpdatedAt().toString()
            );
            messagingTemplate.convertAndSend("/topic/order/" + saved.getId(), payload);
        } catch (Exception e) {
            log.error("Failed to broadcast order update over WS for order {}: {}", saved.getId(), e.getMessage());
        }

        return saved;
    }
}
