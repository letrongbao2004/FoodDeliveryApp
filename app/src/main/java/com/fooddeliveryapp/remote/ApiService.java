package com.fooddeliveryapp.remote;

import com.fooddeliveryapp.models.ChatThread;
import com.fooddeliveryapp.models.ChatMessage;
import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.models.Advertisement;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.models.OrderDetail;
import com.fooddeliveryapp.models.OrderRequest;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.models.User;
import com.fooddeliveryapp.remote.dto.AdUpsertRequest;
import com.fooddeliveryapp.remote.dto.FoodUpsertRequest;
import com.fooddeliveryapp.remote.dto.RegisterRequest;
import com.fooddeliveryapp.remote.dto.RestaurantUpsertRequest;
import com.fooddeliveryapp.remote.dto.UploadResponse;
import retrofit2.Call;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface ApiService {

    // ==== AUTH ====
    @POST("auth/login")
    Call<Map<String, String>> login(@Body Map<String, String> credentials);

    @POST("auth/register")
    Call<Map<String, String>> register(@Body RegisterRequest request);

    // ==== USERS ====
    @GET("users/{id}")
    Call<User> getUserDetails(@Path("id") int id);

    // ==== RESTAURANTS ====
    @GET("restaurants")
    Call<List<Restaurant>> getRestaurants();

    @GET("restaurants/owner/{ownerId}")
    Call<Restaurant> getRestaurantByOwner(@Path("ownerId") long ownerId);

    @POST("restaurants")
    Call<Restaurant> addRestaurant(@Body RestaurantUpsertRequest request);

    @PUT("restaurants/{id}")
    Call<Restaurant> updateRestaurant(@Path("id") long id, @Body RestaurantUpsertRequest request);

    @Multipart
    @POST("uploads/restaurant-logo")
    Call<UploadResponse> uploadRestaurantLogo(@Part MultipartBody.Part file);

    @Multipart
    @POST("upload/image")
    Call<UploadResponse> uploadImage(
            @Part MultipartBody.Part file,
            @Part("context") RequestBody context
    );

    @Multipart
    @POST("upload/image")
    Call<UploadResponse> uploadImageWithEntity(
            @Part MultipartBody.Part file,
            @Part("context") RequestBody context,
            @Part("entity_id") RequestBody entityId
    );

    // ==== BUSINESS HOURS ====
    @PUT("restaurants/{restaurantId}/hours/{day}")
    Call<Object> upsertBusinessHours(@Path("restaurantId") long restaurantId,
                                    @Path("day") String day,
                                    @Body Map<String, Object> body);

    // ==== FOODS ====
    @GET("foods")
    Call<List<Food>> getFoods(@Query("restaurantId") int restaurantId);

    @POST("foods")
    Call<Food> addFood(@Body FoodUpsertRequest request);

    @PUT("foods/{id}/restock")
    Call<Food> restockFood(@Path("id") long id, @Query("amount") int amount);

    @DELETE("foods/{id}")
    Call<Void> deleteFood(@Path("id") long id);

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

    // ==== CHAT ====
    @GET("chat/history")
    Call<List<ChatMessage>> getChatHistory(@Query("userId") long userId, @Query("restaurantId") long restaurantId);

    @GET("chat/threads/user/{userId}")
    Call<List<ChatThread>> getUserChatThreads(@Path("userId") long userId);

    @GET("chat/threads/restaurant/{restaurantId}")
    Call<List<ChatThread>> getRestaurantChatThreads(@Path("restaurantId") long restaurantId);

    // ==== ADS ====
    @GET("ads")
    Call<List<Advertisement>> getAds(@Query("limit") int limit);

    @GET("ads/mine")
    Call<List<Advertisement>> getMyAds();

    @POST("ads")
    Call<Advertisement> createAd(@Body AdUpsertRequest request);

    @PUT("ads/{id}")
    Call<Advertisement> updateAd(@Path("id") long id, @Body AdUpsertRequest request);

    @PATCH("ads/{id}/disable")
    Call<Advertisement> disableAd(@Path("id") long id);
}