package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.managers.OrderManager;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.managers.CartManager;
import com.fooddeliveryapp.utils.SessionManager;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.models.OrderRequest;
import com.fooddeliveryapp.models.OrderItemRequest;
import java.util.ArrayList;
import java.util.List;
import android.widget.RadioGroup;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvSubtotal, tvDelivery, tvTotal, btnPlaceOrder;
    private OrderManager orderManager;
    private SessionManager sessionManager;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // UI Initialization
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        tvSubtotal = findViewById(R.id.tvCheckoutSubtotal);
        tvDelivery = findViewById(R.id.tvCheckoutDelivery);
        tvTotal = findViewById(R.id.tvCheckoutTotal);

        // Core Managers Initialization
        orderManager = OrderManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        cartManager = CartManager.getInstance(this);

        displayOrderSummary();

        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
    }

    private void displayOrderSummary() {
        double subtotal = cartManager.getSubtotal(sessionManager.getUserId());
        double deliveryFee = 1.99;
        double total = subtotal + deliveryFee;

        tvSubtotal.setText(AppUtils.formatPrice(subtotal));
        tvDelivery.setText(AppUtils.formatPrice(deliveryFee));
        tvTotal.setText(AppUtils.formatPrice(total));
    }

    private void handlePlaceOrder() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioGroup rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();

        // Show loading and disable buttons
        setLoadingState(true);

        OrderRequest request = buildOrderRequestFromUI();

        // 🚀 Network Call (Async)
        orderManager.placeOrder(request, new OrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                if (isFinishing() || isDestroyed()) return;
                setLoadingState(false);
                
                if (selectedId == R.id.rbMoMo) {
                    // Order successfully placed! Now redirect to MoMo to pay it.
                    int amountVnd = (int) (order.getTotal() * AppUtils.EXCHANGE_RATE_VND);
                    Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
                    intent.putExtra("amount", amountVnd);
                    startActivity(intent);
                } else {
                    Toast.makeText(CheckoutActivity.this, "Order Confirmed: #" + order.getId(), Toast.LENGTH_LONG).show();
                }
                finish(); // Close activity
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                setLoadingState(false);
                Toast.makeText(CheckoutActivity.this, "Transaction Failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private OrderRequest buildOrderRequestFromUI() {
        long userId = sessionManager.getUserId();
        double deliveryFee = 1.99;
        
        List<com.fooddeliveryapp.models.CartItem> cartItems = cartManager.getCartItems(userId);
        List<OrderItemRequest> itemRequests = new ArrayList<>();
        
        for (com.fooddeliveryapp.models.CartItem item : cartItems) {
            OrderItemRequest req = new OrderItemRequest();
            req.setFoodId((long) item.getFood().getId());
            req.setQuantity(item.getQuantity());
            itemRequests.add(req);
        }

        OrderRequest request = new OrderRequest();
        request.setUserId(userId);
        request.setItems(itemRequests);
        request.setDeliveryFee(deliveryFee);
        return request;
    }

    private Order buildOrderFromUI() {
        // Keeps a preview model for MoMo or UI
        double subtotal = CartManager.getInstance(this).getSubtotal(sessionManager.getUserId());
        double deliveryFee = 1.99;
        double total = subtotal + deliveryFee;

        Order order = new Order();
        order.setUserId((int) sessionManager.getUserId());
        order.setRestaurantId(1);
        order.setTotal(total);
        order.setDeliveryAddress("123 Cloud Server Lane");
        return order;
    }

    private void setLoadingState(boolean isLoading) {
        if (btnPlaceOrder != null) {
            btnPlaceOrder.setEnabled(!isLoading);
        }
    }
}
