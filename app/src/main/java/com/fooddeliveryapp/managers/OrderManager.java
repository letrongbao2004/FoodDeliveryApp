package com.fooddeliveryapp.managers;

import android.content.Context;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.models.OrderRequest;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

/**
 * OrderManager – handles creating and tracking orders purely through the Network API.
 */
public class OrderManager {

    private static OrderManager instance;
    private final ApiService apiService;

    private OrderManager(Context context) {
        this.apiService = ApiClient.getClient(context).create(ApiService.class);
    }

    public static synchronized OrderManager getInstance(Context context) {
        if (instance == null) {
            instance = new OrderManager(context.getApplicationContext());
        }
        return instance;
    }

    public interface OrderCallback {
        void onSuccess(Order order);
        void onError(String message);
    }

    public interface OrderListCallback {
        void onSuccess(List<Order> orders);
        void onError(String message);
    }

    /**
     * Places an order completely via server.
     */
    public void placeOrder(OrderRequest request, OrderCallback callback) {
        apiService.placeOrder(request).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Order Failed: Server returned " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }

    /**
     * Fetches the order history for a particular user from the server.
     */
    public void getOrderHistory(long userId, OrderListCallback callback) {
        apiService.getUserOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch history");
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }

    public void getOrderById(int orderId, OrderCallback callback) {
        apiService.getOrderById(orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError("Fail: " + response.code());
            }
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    public void getRestaurantOrders(int restaurantId, OrderListCallback callback) {
        apiService.getRestaurantOrders(restaurantId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError("Fail: " + response.code());
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                callback.onError("Network: " + t.getMessage());
            }
        });
    }

    public void updateOrderStatus(int orderId, String status, OrderCallback callback) {
        apiService.updateOrderStatus(orderId, status).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError("Fail: " + response.code());
            }
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                callback.onError("Network: " + t.getMessage());
            }
        });
    }
}
