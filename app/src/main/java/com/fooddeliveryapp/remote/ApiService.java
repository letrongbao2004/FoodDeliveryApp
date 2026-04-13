package com.fooddeliveryapp.remote;

import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.models.OrderDetail;
import com.fooddeliveryapp.models.OrderRequest;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.models.User;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface ApiService {

    // ==== AUTH ====
    @POST("auth/login")
    Call<Map<String, String>> login(@Body Map<String, String> credentials);

    @POST("auth/register")
    Call<Map<String, String>> register(@Body User user);

    // ==== USERS ====
    @GET("users/{id}")
    Call<User> getUserDetails(@Path("id") int id);

    // ==== RESTAURANTS ====
    @GET("restaurants")
    Call<List<Restaurant>> getRestaurants();

    // ==== FOODS ====
    @GET("foods")
    Call<List<Food>> getFoods(@Query("restaurantId") int restaurantId);

    // ==== ORDERS ====
    @POST("orders")
    Call<Order> placeOrder(@Body OrderRequest request);

    @GET("orders/user/{userId}")
    Call<List<Order>> getUserOrders(@Path("userId") long userId);

    @GET("orders/{id}")
    Call<Order> getOrderById(@Path("id") long id);

    /** Full order detail with items list */
    @GET("orders/{id}/detail")
    Call<OrderDetail> getOrderDetail(@Path("id") long id);

    @GET("orders/restaurant/{id}")
    Call<List<Order>> getRestaurantOrders(@Path("id") int id);

    @PUT("orders/{id}/status")
    Call<Order> updateOrderStatus(@Path("id") long id, @Query("status") String status);
}