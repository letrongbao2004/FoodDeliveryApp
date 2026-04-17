package com.fooddeliveryapp.viewmodels;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.fooddeliveryapp.models.OrderStatus;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.remote.WebSocketManager;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderTrackingViewModel extends AndroidViewModel {

    private final MutableLiveData<OrderStatus> orderStatusLiveData = new MutableLiveData<>();
    private final WebSocketManager wsManager;
    private final ApiService apiService;
    private long currentOrderId = -1;

    public OrderTrackingViewModel(@NonNull Application application) {
        super(application);
        wsManager = WebSocketManager.getInstance();
        apiService = ApiClient.getClient(application).create(ApiService.class);
    }

    public LiveData<OrderStatus> getOrderStatusLiveData() {
        return orderStatusLiveData;
    }

    public void connectAndSubscribe(long orderId) {
        this.currentOrderId = orderId;
        wsManager.connect();
        
        // Cần delay nhỏ hoặc lắng nghe khi connected, nhưng thư viện tự handle queue.
        wsManager.subscribeToOrder(orderId, payload -> {
            try {
                JSONObject json = new JSONObject(payload);
                String statusStr = json.optString("status");
                if (!statusStr.isEmpty()) {
                    try {
                        OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
                        orderStatusLiveData.postValue(status);
                    } catch (IllegalArgumentException e) {
                        Log.e("OrderTrackingVM", "Unknown status received: " + statusStr);
                    }
                }
            } catch (Exception e) {
                Log.e("OrderTrackingVM", "Error parsing incoming WS message", e);
            }
        });
    }
    
    // Explicit Sync called during Activity onResume
    public void setExplicitStatus(OrderStatus status) {
        orderStatusLiveData.postValue(status);
    }

    public void sendStatusUpdate(OrderStatus nextStatus, String role) {
        if (currentOrderId != -1) {
            // Priority 1: REST call for Source of Truth
            apiService.updateOrderStatus(currentOrderId, nextStatus.name()).enqueue(new Callback<com.fooddeliveryapp.models.Order>() {
                @Override
                public void onResponse(Call<com.fooddeliveryapp.models.Order> call, Response<com.fooddeliveryapp.models.Order> response) {
                    if (response.isSuccessful()) {
                        // Success - Optionally post UI update immediately or wait for WS confirmation
                        orderStatusLiveData.postValue(nextStatus);
                    } else {
                        Log.e("OrderTrackingVM", "Failed to update status over REST");
                    }
                }

                @Override
                public void onFailure(Call<com.fooddeliveryapp.models.Order> call, Throwable t) {
                    Log.e("OrderTrackingVM", "Network error updating status", t);
                }
            });

            // Priority 2: WS for live broadcast (if connected)
            if (wsManager != null) {
                wsManager.sendOrderStatusUpdate(currentOrderId, nextStatus.name(), role);
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        wsManager.disconnect();
    }
}
