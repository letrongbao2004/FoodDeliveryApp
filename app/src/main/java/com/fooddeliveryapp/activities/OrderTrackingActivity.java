package com.fooddeliveryapp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.managers.OrderManager;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.utils.AppUtils;

public class OrderTrackingActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    private OrderManager orderManager;
    private Order order;

    private TextView tvOrderCode, tvStatus, tvRestaurantName,
                     tvTotal, tvAddress, tvPayment, tvEstimatedTime;
    private ProgressBar progressTracking;
    private View stepPending, stepConfirmed, stepPreparing, stepDelivering, stepDelivered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderManager = OrderManager.getInstance(this);

        int orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
        if (orderId == -1) { finish(); return; }

        orderManager.getOrderById(orderId, new OrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order o) {
                order = o;
                populateOrder();
                simulateOrderProgress();
            }

            @Override
            public void onError(String message) {
                AppUtils.showToast(OrderTrackingActivity.this, "Order not found");
                finish();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Order Tracking");
        }

        bindViews();
        // Logic executed via async callback now
    }

    private void bindViews() {
        tvOrderCode      = findViewById(R.id.tvTrackOrderCode);
        tvStatus         = findViewById(R.id.tvTrackStatus);
        tvRestaurantName = findViewById(R.id.tvTrackRestaurantName);
        tvTotal          = findViewById(R.id.tvTrackTotal);
        tvAddress        = findViewById(R.id.tvTrackAddress);
        tvPayment        = findViewById(R.id.tvTrackPayment);
        tvEstimatedTime  = findViewById(R.id.tvEstimatedTime);
        progressTracking = findViewById(R.id.progressTracking);
        stepPending      = findViewById(R.id.stepPending);
        stepConfirmed    = findViewById(R.id.stepConfirmed);
        stepPreparing    = findViewById(R.id.stepPreparing);
        stepDelivering   = findViewById(R.id.stepDelivering);
        stepDelivered    = findViewById(R.id.stepDelivered);
    }

    private void populateOrder() {
        tvOrderCode.setText(order.getOrderCode());
        tvStatus.setText(order.getStatus());
        tvRestaurantName.setText(order.getRestaurantName());
        tvTotal.setText(order.getTotalText());
        tvAddress.setText(order.getDeliveryAddress());
        tvPayment.setText(order.getPaymentMethod());
        tvEstimatedTime.setText("Estimated delivery: 30-45 mins");
        updateStepIndicators(order.getStatus());
    }

    private void updateStepIndicators(String status) {
        int activeColor   = getColor(R.color.primary);
        int inactiveColor = getColor(R.color.divider);

        stepPending.setBackgroundColor(activeColor);
        stepConfirmed.setBackgroundColor(
                statusAtLeast(status, Order.STATUS_CONFIRMED) ? activeColor : inactiveColor);
        stepPreparing.setBackgroundColor(
                statusAtLeast(status, Order.STATUS_PREPARING) ? activeColor : inactiveColor);
        stepDelivering.setBackgroundColor(
                statusAtLeast(status, Order.STATUS_DELIVERING) ? activeColor : inactiveColor);
        stepDelivered.setBackgroundColor(
                statusAtLeast(status, Order.STATUS_DELIVERED) ? activeColor : inactiveColor);

        int progress = getProgressForStatus(status);
        progressTracking.setProgress(progress);
        tvStatus.setTextColor(getColor(AppUtils.getStatusColor(status)));
    }

    private boolean statusAtLeast(String current, String target) {
        return getProgressForStatus(current) >= getProgressForStatus(target);
    }

    private int getProgressForStatus(String status) {
        switch (status) {
            case Order.STATUS_PENDING:    return 10;
            case Order.STATUS_CONFIRMED:  return 30;
            case Order.STATUS_PREPARING:  return 55;
            case Order.STATUS_DELIVERING: return 80;
            case Order.STATUS_DELIVERED:  return 100;
            default: return 0;
        }
    }

    private void simulateOrderProgress() {
        // In production, this would use WebSocket or polling.
        // Here we simulate auto-advancement every 5 seconds for demo.
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        String[] statusFlow = {
            Order.STATUS_CONFIRMED, Order.STATUS_PREPARING,
            Order.STATUS_DELIVERING, Order.STATUS_DELIVERED
        };
        for (int i = 0; i < statusFlow.length; i++) {
            final String nextStatus = statusFlow[i];
            final int orderId = order.getId();
            handler.postDelayed(() -> {
                orderManager.updateOrderStatus(orderId, nextStatus, new OrderManager.OrderCallback() {
                    @Override
                    public void onSuccess(Order updatedOrder) {
                        order.setStatus(updatedOrder.getStatus());
                        tvStatus.setText(updatedOrder.getStatus());
                        updateStepIndicators(updatedOrder.getStatus());
                    }

                    @Override
                    public void onError(String message) {}
                });
                // ui steps handled by callback
            }, (long) (i + 1) * 5000);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
