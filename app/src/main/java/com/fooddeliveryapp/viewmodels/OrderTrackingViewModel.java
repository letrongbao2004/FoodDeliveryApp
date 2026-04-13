package com.fooddeliveryapp.viewmodels;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.fooddeliveryapp.models.OrderStatus;
import com.fooddeliveryapp.remote.WebSocketManager;
import org.json.JSONObject;

public class OrderTrackingViewModel extends AndroidViewModel {

    private final MutableLiveData<OrderStatus> orderStatusLiveData = new MutableLiveData<>();
    private final WebSocketManager wsManager;
    private long currentOrderId = -1;

    public OrderTrackingViewModel(@NonNull Application application) {
        super(application);
        wsManager = WebSocketManager.getInstance();
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
                    OrderStatus status = OrderStatus.valueOf(statusStr);
                    orderStatusLiveData.postValue(status);
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
            wsManager.sendOrderStatusUpdate(currentOrderId, nextStatus.name(), role);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        wsManager.disconnect();
    }
}
