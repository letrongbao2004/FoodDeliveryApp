package com.fooddeliveryapp.api.services;

import com.fooddeliveryapp.api.dto.*;
import com.fooddeliveryapp.api.models.*;
import com.fooddeliveryapp.api.repositories.*;
import com.fooddeliveryapp.api.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // Lock waits up to 10s to acquire; auto-releases after 30s as a safety net
    private static final long LOCK_WAIT_TIME  = 10;
    private static final long LOCK_LEASE_TIME = 30;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final DistributedLockService distributedLockService;

    /**
     * Self-injection via @Lazy breaks the circular reference and lets Spring
     * apply the @Transactional AOP proxy on placeOrderTransactional() when it
     * is called from inside the distributed-lock lambda.
     */
    @Autowired
    @Lazy
    private OrderService self;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        FoodRepository foodRepository,
                        RestaurantRepository restaurantRepository,
                        ChatService chatService,
                        SimpMessagingTemplate messagingTemplate,
                        DistributedLockService distributedLockService) {
        this.orderRepository        = orderRepository;
        this.userRepository         = userRepository;
        this.foodRepository         = foodRepository;
        this.restaurantRepository   = restaurantRepository;
        this.chatService            = chatService;
        this.messagingTemplate      = messagingTemplate;
        this.distributedLockService = distributedLockService;
    }

    // -----------------------------------------------------------------------
    // PUBLIC API
    // -----------------------------------------------------------------------

    /**
     * Entry point for placing an order.
     *
     * Acquires a per-restaurant distributed lock BEFORE opening the database
     * transaction.  This guarantees that concurrent requests for the same
     * restaurant are serialised, preventing negative stock quantities even in
     * a multi-instance (horizontally-scaled) deployment.
     *
     * Flow:
     *   1. Resolve restaurantId (lightweight, may do 1 DB read).
     *   2. Try to acquire Redisson lock for "order:restaurant:<id>".
     *   3. If acquired → call self.placeOrderTransactional() through the
     *      Spring proxy so @Transactional is honoured.
     *   4. Lock is released in the finally block of DistributedLockService
     *      AFTER the transaction has committed (or rolled back).
     *   5. If lock cannot be acquired → LockBusyException → HTTP 409.
     */
    public Order placeOrder(OrderRequest request) {
        Long restaurantId = resolveRestaurantId(request);
        String lockKey    = "order:restaurant:" + restaurantId;

        log.info("[OrderService] placeOrder – user={} restaurant={} lockKey={}",
                request.getUserId(), restaurantId, lockKey);

        return distributedLockService.executeWithLock(
                lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS,
                () -> self.placeOrderTransactional(request)
        );
    }

    // -----------------------------------------------------------------------
    // TRANSACTIONAL INNER METHOD (called through Spring proxy via self)
    // -----------------------------------------------------------------------

    /**
     * Executes the actual order placement inside a database transaction.
     * Must only be called while the distributed lock for this restaurant is held.
     *
     * NOTE: public visibility is required so the Spring AOP proxy can intercept
     * it and apply the @Transactional behaviour.
     */
    @Transactional
    public Order placeOrderTransactional(OrderRequest request) {

        // --- Resolve user ---
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // --- Resolve restaurant ---
        Restaurant restaurant = null;
        if (request.getRestaurantId() != null && request.getRestaurantId() > 0) {
            restaurant = restaurantRepository.findById(request.getRestaurantId()).orElse(null);
        }
        if (restaurant == null && request.getItems() != null && !request.getItems().isEmpty()) {
            Long firstFoodId = request.getItems().get(0).getFoodId();
            Food firstFood = foodRepository.findById(firstFoodId).orElse(null);
            if (firstFood != null) {
                restaurant = firstFood.getRestaurant();
            }
        }

        // --- Build order shell ---
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(OrderStatus.ORDER_PLACED);
        order.setOrderDate(new Date());
        order.setDeliveryFee(request.getDeliveryFee());

        // --- Batch-load all foods in one query ---
        Set<Long> foodIds = request.getItems().stream()
                .map(OrderRequest.OrderItemRequest::getFoodId)
                .collect(Collectors.toSet());

        Map<Long, Food> foodMap = foodRepository.findAllById(foodIds).stream()
                .collect(Collectors.toMap(Food::getId, Function.identity()));

        // --- Process each line item (CRITICAL SECTION – lock is held) ---
        double subtotal = 0;
        List<OrderItem> items = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Food food = foodMap.get(itemReq.getFoodId());
            if (food == null) {
                throw new ResourceNotFoundException("Food not found: " + itemReq.getFoodId());
            }

            if (food.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException(
                        "Sản phẩm '" + food.getName() + "' đã hết hoặc không đủ số lượng trong kho.");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setFood(food);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(food.getPrice());
            items.add(item);

            subtotal += item.getPrice() * item.getQuantity();
            food.setOrderCount(food.getOrderCount() + item.getQuantity());
            food.setStockQuantity(food.getStockQuantity() - itemReq.getQuantity());
        }

        foodRepository.saveAll(foodMap.values());
        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal + order.getDeliveryFee());

        Order saved = orderRepository.save(order);

        // --- Post-order: send chat confirmation (non-critical, swallow errors) ---
        final Long restId = restaurant != null ? restaurant.getId() : 0L;
        try {
            chatService.sendOrderConfirmation(user.getId(), restId, saved.getId());
        } catch (Exception e) {
            log.error("[OrderService] Failed to send order confirmation chat for order {}: {}",
                    saved.getId(), e.getMessage());
        }

        return saved;
    }

    // -----------------------------------------------------------------------
    // READ-ONLY OPERATIONS (no lock needed)
    // -----------------------------------------------------------------------

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

        if (status == OrderStatus.CANCELLED) {
            if (order.getStatus() != OrderStatus.ORDER_PLACED
                    && order.getStatus() != OrderStatus.ORDER_PACKED) {
                throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
            }
        }

        order.setStatus(status);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        Order saved = orderRepository.save(order);

        try {
            Map<String, Object> payload = Map.of(
                    "orderId",   saved.getId(),
                    "status",    status.name(),
                    "updatedAt", saved.getUpdatedAt().toString()
            );
            messagingTemplate.convertAndSend("/topic/order/" + saved.getId(), payload);
        } catch (Exception e) {
            log.error("[OrderService] Failed to broadcast order update for order {}: {}",
                    saved.getId(), e.getMessage());
        }

        return saved;
    }

    // -----------------------------------------------------------------------
    // PRIVATE HELPERS
    // -----------------------------------------------------------------------

    /**
     * Determines the restaurant ID to use as a lock-key discriminator.
     * Avoids opening a transaction just for this—falls back to a single
     * foodRepository.findById() when restaurantId is absent from the request.
     */
    private Long resolveRestaurantId(OrderRequest request) {
        if (request.getRestaurantId() != null && request.getRestaurantId() > 0) {
            return request.getRestaurantId();
        }
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            Long firstFoodId = request.getItems().get(0).getFoodId();
            return foodRepository.findById(firstFoodId)
                    .map(f -> f.getRestaurant() != null ? f.getRestaurant().getId() : 0L)
                    .orElse(0L);
        }
        return 0L;
    }
}
