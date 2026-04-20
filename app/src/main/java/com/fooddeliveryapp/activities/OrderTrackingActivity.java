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
import com.fooddeliveryapp.models.OrderStatus;
import com.fooddeliveryapp.utils.AppUtils;

public class OrderTrackingActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    private OrderManager orderManager;
    private Order order;
    private android.os.Handler handler;

    private TextView tvOrderCode, tvStatus, tvRestaurantName,
            tvTotal, tvAddress, tvPayment, tvEstimatedTime;
    private ProgressBar progressTracking;
    private View stepPending, stepConfirmed, stepPreparing, stepDelivering, stepDelivered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderManager = OrderManager.getInstance(this);
        handler = new android.os.Handler(android.os.Looper.getMainLooper());

        long orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1L);
        if (orderId == -1L) {
            orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
            if (orderId == -1) {
                finish();
                return;
            }
        }

        orderManager.getOrderById(orderId, new OrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order o) {
                if (isFinishing() || isDestroyed())
                    return;
                order = o;
                populateOrder();
                simulateOrderProgress();
            }

            @Override
            public void onError(String message, int httpCode) {
                if (isFinishing() || isDestroyed())
                    return;
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
        tvOrderCode = findViewById(R.id.tvTrackOrderCode);
        tvStatus = findViewById(R.id.tvTrackStatus);
        tvRestaurantName = findViewById(R.id.tvTrackRestaurantName);
        tvTotal = findViewById(R.id.tvTrackTotal);
        tvAddress = findViewById(R.id.tvTrackAddress);
        tvPayment = findViewById(R.id.tvTrackPayment);
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime);
        progressTracking = findViewById(R.id.progressTracking);
        stepPending = findViewById(R.id.stepPending);
        stepConfirmed = findViewById(R.id.stepConfirmed);
        stepPreparing = findViewById(R.id.stepPreparing);
        stepDelivering = findViewById(R.id.stepDelivering);
        stepDelivered = findViewById(R.id.stepDelivered);
    }

    private void populateOrder() {
        tvOrderCode.setText(order.getOrderCode());
        tvStatus.setText(order.getStatus() != null ? order.getStatus().name() : "");
        tvRestaurantName.setText(order.getRestaurantName());
        tvTotal.setText(order.getTotalText());
        tvAddress.setText(order.getDeliveryAddress());
        tvPayment.setText(order.getPaymentMethod());
        tvEstimatedTime.setText("Estimated delivery: 30-45 mins");
        updateStepIndicators(order.getStatus());
    }

    private void updateStepIndicators(OrderStatus status) {
        if (status == null) return;
        int activeColor = getColor(R.color.primary);
        int inactiveColor = getColor(R.color.divider);

        stepPending.setBackgroundColor(activeColor);
        stepConfirmed.setBackgroundColor(
                statusAtLeast(status, OrderStatus.ORDER_PACKED) ? activeColor : inactiveColor);
        stepPreparing.setBackgroundColor(
                statusAtLeast(status, OrderStatus.ORDER_PACKED) ? activeColor : inactiveColor);
        stepDelivering.setBackgroundColor(
                statusAtLeast(status, OrderStatus.OUT_FOR_DELIVERY) ? activeColor : inactiveColor);
        stepDelivered.setBackgroundColor(
                statusAtLeast(status, OrderStatus.DELIVERED) ? activeColor : inactiveColor);

        int progress = getProgressForStatus(status);
        progressTracking.setProgress(progress);
        tvStatus.setTextColor(getColor(AppUtils.getStatusColor(status)));
    }

    private boolean statusAtLeast(OrderStatus current, OrderStatus target) {
        return getProgressForStatus(current) >= getProgressForStatus(target);
    }

    private int getProgressForStatus(OrderStatus status) {
        if (status == null) return 0;
        switch (status) {
            case ORDER_PLACED:
                return 10;
            case ORDER_PACKED:
                return 40;
            case OUT_FOR_DELIVERY:
                return 75;
            case DELIVERED:
                return 100;
            default:
                return 0;
        }
    }

    private void simulateOrderProgress() {
        OrderStatus[] statusFlow = {
                OrderStatus.ORDER_PACKED,
                OrderStatus.OUT_FOR_DELIVERY, 
                OrderStatus.DELIVERED
        };
        for (int i = 0; i < statusFlow.length; i++) {
            final OrderStatus nextStatus = statusFlow[i];
            final long orderId = order.getId();
            handler.postDelayed(() -> {
                orderManager.updateOrderStatus(orderId, nextStatus.name(), new OrderManager.OrderCallback() {
                    @Override
                    public void onSuccess(Order updatedOrder) {
                        if (isFinishing() || isDestroyed())
                            return;
                        order.setStatus(updatedOrder.getStatus());
                        tvStatus.setText(updatedOrder.getStatus() != null ? updatedOrder.getStatus().name() : "");
                        updateStepIndicators(updatedOrder.getStatus());
                    }

                    @Override
                    public void onError(String message, int httpCode) {
                        if (isFinishing() || isDestroyed())
                            return;
                    }
                });
            }, (long) (i + 1) * 5000);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}