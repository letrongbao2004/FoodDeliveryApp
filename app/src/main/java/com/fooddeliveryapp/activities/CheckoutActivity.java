package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.managers.CartManager;
import com.fooddeliveryapp.managers.OrderManager;
import com.fooddeliveryapp.models.CartItem;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.models.OrderItemRequest;
import com.fooddeliveryapp.models.OrderRequest;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.SessionManager;

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
        tvSubtotal    = findViewById(R.id.tvCheckoutSubtotal);
        tvDelivery    = findViewById(R.id.tvCheckoutDelivery);
        tvTotal       = findViewById(R.id.tvCheckoutTotal);

        // Core Managers
        orderManager  = OrderManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        cartManager   = CartManager.getInstance(this);

        displayOrderSummary();

        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
    }

    // ---------------------------------------------------------------
    // Display summary
    // ---------------------------------------------------------------
    private void displayOrderSummary() {
        long userId = sessionManager.getUserId();
        double subtotal    = cartManager.getSubtotal(userId);
        double deliveryFee = 1.99;
        double total       = subtotal + deliveryFee;

        tvSubtotal.setText(AppUtils.formatPrice(subtotal));
        tvDelivery.setText(AppUtils.formatPrice(deliveryFee));
        tvTotal   .setText(AppUtils.formatPrice(total));
    }

    // ---------------------------------------------------------------
    // Place order flow
    // ---------------------------------------------------------------
    private void handlePlaceOrder() {
        // 1. Auth guard
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Cart guard
        long userId = sessionManager.getUserId();
        List<CartItem> cartItems = cartManager.getCartItems(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Read selected payment method
        RadioGroup rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();

        // 4. Show loading
        setLoadingState(true);

        // 5. Build request and call API
        OrderRequest request = buildOrderRequest(userId, cartItems);

        orderManager.placeOrder(request, new OrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    setLoadingState(false);

                    // ✅ Clear cart after successful order
                    cartManager.clearCart(userId);

                    if (selectedId == R.id.rbMoMo) {
                        // Redirect to MoMo payment
                        int amountVnd = (int) (order.getTotal() * AppUtils.EXCHANGE_RATE_VND);
                        Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
                        intent.putExtra("amount",  amountVnd);
                        intent.putExtra("orderId", order.getId());
                        startActivity(intent);
                    } else {
                        Toast.makeText(CheckoutActivity.this,
                                "Order Confirmed: " + order.getOrderCode(),
                                Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(CheckoutActivity.this, OrderDetailActivity.class);
                        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId());
                        startActivity(intent);
                    }
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    setLoadingState(false);
                    Toast.makeText(CheckoutActivity.this,
                            "Transaction Failed: " + message,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ---------------------------------------------------------------
    // Build OrderRequest from current cart state
    // ---------------------------------------------------------------
    private OrderRequest buildOrderRequest(long userId, List<CartItem> cartItems) {
        List<OrderItemRequest> itemRequests = new ArrayList<>();

        // Extract restaurantId from the first cart item
        long restaurantId = 0;
        for (CartItem item : cartItems) {
            if (item.getFood() != null) {
                if (restaurantId == 0) {
                    restaurantId = item.getFood().getRestaurantId();
                }
                OrderItemRequest req = new OrderItemRequest();
                req.setFoodId((long) item.getFood().getId());
                req.setQuantity(item.getQuantity());
                itemRequests.add(req);
            }
        }

        // Delivery address from user profile (fallback to default)
        String address = sessionManager.getAddress();
        if (address == null || address.isEmpty()) {
            address = "Địa chỉ chưa được cài đặt";
        }

        OrderRequest request = new OrderRequest();
        request.setUserId(userId);
        request.setRestaurantId(restaurantId);
        request.setItems(itemRequests);
        request.setDeliveryFee(1.99);
        request.setDeliveryAddress(address);
        return request;
    }

    // ---------------------------------------------------------------
    // Loading state helper
    // ---------------------------------------------------------------
    private void setLoadingState(boolean isLoading) {
        if (btnPlaceOrder != null) {
            btnPlaceOrder.setEnabled(!isLoading);
            btnPlaceOrder.setText(isLoading ? "Đang xử lý..." : "Place Order");
        }
    }
}