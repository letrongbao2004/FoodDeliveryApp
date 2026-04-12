package com.fooddeliveryapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.managers.OrderManager;
import com.fooddeliveryapp.managers.SessionManager;
import com.fooddeliveryapp.models.Order;

public class CheckoutActivity extends AppCompatActivity {

    private TextView btnPlaceOrder;
    private OrderManager orderManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // UI Initialization
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Core Managers Initialization
        orderManager = OrderManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
    }

    private void handlePlaceOrder() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading and disable buttons
        setLoadingState(true);

        Order newOrder = buildOrderFromUI();

        // 🚀 Network Call (Async)
        orderManager.placeOrder(newOrder, new OrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                // UI Thread executes here
                setLoadingState(false);
                Toast.makeText(CheckoutActivity.this, "Order Confirmed: #" + order.getId(), Toast.LENGTH_LONG).show();
                finish(); // Close activity and go back to home screen
            }

            @Override
            public void onError(String message) {
                // UI Thread executes here
                setLoadingState(false);
                Toast.makeText(CheckoutActivity.this, "Transaction Failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Order buildOrderFromUI() {
        // Collects mock or layout data. Simplified for example.
        Order order = new Order();
        order.setUserId(sessionManager.getUserId());
        order.setRestaurantId(1);
        order.setTotal(45.99);
        order.setDeliveryAddress("123 Cloud Server Lane");
        return order;
    }

    private void setLoadingState(boolean isLoading) {
        if (btnPlaceOrder != null) {
            btnPlaceOrder.setEnabled(!isLoading);
        }
    }
}
