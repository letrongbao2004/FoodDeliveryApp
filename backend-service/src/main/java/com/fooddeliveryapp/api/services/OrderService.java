package com.fooddeliveryapp.api.services;

import com.fooddeliveryapp.api.dto.OrderRequest;
import com.fooddeliveryapp.api.models.Food;
import com.fooddeliveryapp.api.models.Order;
import com.fooddeliveryapp.api.models.OrderItem;
import com.fooddeliveryapp.api.models.User;
import com.fooddeliveryapp.api.repositories.FoodRepository;
import com.fooddeliveryapp.api.repositories.OrderRepository;
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

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, FoodRepository foodRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
    }

    // @Transactional fixes partial updates and dangling logic
    @Transactional
    public Order placeOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
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
}
