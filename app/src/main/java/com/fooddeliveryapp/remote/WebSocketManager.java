package com.fooddeliveryapp.remote;

import android.util.Log;
import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class WebSocketManager {

    private static WebSocketManager instance;
    private StompClient stompClient;
    private Disposable lifecycleDisposable;
    private Disposable topicDisposable;

    private static final String TAG = "WebSocketManager";
    // Use host machine LAN IP for real-device testing.
    private static final String WS_URL = "ws://192.168.1.7/ws/websocket";

    private WebSocketManager() {
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void connect() {
        if (stompClient == null || !stompClient.isConnected()) {
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);
            
            lifecycleDisposable = stompClient.lifecycle().subscribe(lifecycleEvent -> {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.i(TAG, "Stomp connection opened");
                        break;
                    case ERROR:
                        Log.e(TAG, "Stomp connection error! Check if server is running at: " + WS_URL, lifecycleEvent.getException());
                        break;
                    case CLOSED:
                        Log.w(TAG, "Stomp connection closed. Retrying on next action.");
                        break;
                }
            });
            
            stompClient.connect();
        }
    }

    public void subscribeToOrder(long orderId, final MessageListener listener) {
        if (stompClient == null || !stompClient.isConnected()) return;

        if (topicDisposable != null && !topicDisposable.isDisposed()) {
            topicDisposable.dispose();
        }

        topicDisposable = stompClient.topic("/topic/order/" + orderId)
                .subscribe(stompMessage -> {
                    listener.onMessageReceived(stompMessage.getPayload());
                }, throwable -> {
                    Log.e(TAG, "Error on subscribe", throwable);
                });
    }

    public void sendOrderStatusUpdate(long orderId, String newStatus, String updatedBy) {
        if (stompClient != null && stompClient.isConnected()) {
            String payload = "{\"orderId\":" + orderId + ",\"newStatus\":\"" + newStatus + "\",\"updatedBy\":\"" + updatedBy + "\"}";
            stompClient.send("/app/order/update-status", payload).subscribe(() -> {
                Log.d(TAG, "Successfully sent update via WS");
            }, throwable -> {
                Log.e(TAG, "Error sending WS update", throwable);
            });
        }
    }

    public void disconnect() {
        if (topicDisposable != null) topicDisposable.dispose();
        if (lifecycleDisposable != null) lifecycleDisposable.dispose();
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
    }

    public interface MessageListener {
        void onMessageReceived(String payload);
    }
}
