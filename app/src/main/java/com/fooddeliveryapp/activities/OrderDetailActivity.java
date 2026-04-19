package com.fooddeliveryapp.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.OrderDetailAdapter;
import com.fooddeliveryapp.models.OrderDetail;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.lifecycle.ViewModelProvider;
import com.fooddeliveryapp.viewmodels.OrderTrackingViewModel;
import com.fooddeliveryapp.views.OrderProgressView;
import com.fooddeliveryapp.models.OrderStatus;
import android.widget.Button;
import com.fooddeliveryapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    private TextView tvOrderCode, tvStatus, tvDate, tvSubtotal, tvDeliveryFee, tvTotal;
    private RecyclerView rvItems;
    private ProgressBar progressBar;
    private View scrollView;
    
    private OrderProgressView orderProgressView;
    private Button btnTrackingActionPrimary;
    private OrderTrackingViewModel trackingViewModel;
    private SessionManager sessionManager;
    private long currentOrderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Toolbar toolbar = findViewById(R.id.toolbarOrderDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Order Detail");
        }

        tvOrderCode = findViewById(R.id.tvDetailOrderCode);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvDate = findViewById(R.id.tvDetailDate);
        tvSubtotal = findViewById(R.id.tvDetailSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDetailDeliveryFee);
        tvTotal = findViewById(R.id.tvDetailTotal);
        rvItems = findViewById(R.id.rvOrderDetailItems);
        progressBar = findViewById(R.id.progressBarOrderDetail);
        scrollView = findViewById(R.id.scrollViewOrderDetail);

        long orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1L);
        if (orderId == -1L) {
            orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
            if (orderId == -1) {
                finish();
                return;
            }
        }
        this.currentOrderId = orderId;

        orderProgressView = findViewById(R.id.orderProgressView);
        btnTrackingActionPrimary = findViewById(R.id.btnTrackingActionPrimary);
        
        sessionManager = SessionManager.getInstance(this);
        trackingViewModel = new ViewModelProvider(this).get(OrderTrackingViewModel.class);

        // Observe WS state changes
        trackingViewModel.getOrderStatusLiveData().observe(this, this::updateUIForStatus);

        // Initiate connection
        trackingViewModel.connectAndSubscribe(orderId);

        loadOrderDetail(orderId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentOrderId != -1) {
            loadOrderDetail(currentOrderId); // explicit RESYNC
        }
    }

    private void loadOrderDetail(long orderId) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (scrollView != null) scrollView.setVisibility(View.GONE);

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getOrderDetail(orderId).enqueue(new Callback<OrderDetail>() {
            @Override
            public void onResponse(Call<OrderDetail> call, Response<OrderDetail> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (scrollView != null) scrollView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    bindDetail(response.body());
                } else {
                    AppUtils.showToast(OrderDetailActivity.this, "Could not load order detail");
                }
            }

            @Override
            public void onFailure(Call<OrderDetail> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
                AppUtils.showToast(OrderDetailActivity.this, "Network error: " + t.getMessage());
            }
        });
    }

    private void bindDetail(OrderDetail detail) {
        tvOrderCode.setText(detail.getOrderCode());
        tvStatus.setText(detail.getStatus() != null ? detail.getStatus().name() : "");

        // Status background color
        int statusColor = AppUtils.getStatusColor(detail.getStatus());
        tvStatus.setBackgroundColor(getColor(statusColor));

        // Sync ViewModel with fetched state explicitly
        if (detail.getStatus() != null) {
            trackingViewModel.setExplicitStatus(detail.getStatus());
        }

        // Date
        if (detail.getOrderDate() != null) {
            String dateStr = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
                    .format(detail.getOrderDate());
            tvDate.setText(dateStr);
        }

        // Prices
        tvSubtotal.setText(AppUtils.formatPrice(detail.getSubtotal()));
        tvDeliveryFee.setText(AppUtils.formatPrice(detail.getDeliveryFee()));
        tvTotal.setText(AppUtils.formatPrice(detail.getTotal()));

        // Items
        if (detail.getItems() != null && !detail.getItems().isEmpty()) {
            OrderDetailAdapter adapter = new OrderDetailAdapter(this, detail.getItems());
            rvItems.setLayoutManager(new LinearLayoutManager(this));
            rvItems.setAdapter(adapter);
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

    private void updateUIForStatus(OrderStatus status) {
        if (status == null) return;

        // Visual Slider
        orderProgressView.updateProgress(status);
        
        // Header fallback
        tvStatus.setText(status.name());
        tvStatus.setBackgroundColor(getColor(AppUtils.getStatusColor(status)));

        // Action Buttons Setup based on Role
        btnTrackingActionPrimary.setVisibility(View.GONE);
        String role = sessionManager.getRole(); // Assuming getRole() exists or we map boolean

        if ("MERCHANT".equalsIgnoreCase(role)) {
            if (status == OrderStatus.ORDER_PLACED) {
                btnTrackingActionPrimary.setVisibility(View.VISIBLE);
                btnTrackingActionPrimary.setText("Xác nhận & Đóng gói");
                btnTrackingActionPrimary.setOnClickListener(v -> trackingViewModel.sendStatusUpdate(OrderStatus.ORDER_PACKED, "MERCHANT"));
            } else if (status == OrderStatus.ORDER_PACKED) {
                btnTrackingActionPrimary.setVisibility(View.VISIBLE);
                btnTrackingActionPrimary.setText("Giao hàng cho Shipper");
                btnTrackingActionPrimary.setOnClickListener(v -> trackingViewModel.sendStatusUpdate(OrderStatus.OUT_FOR_DELIVERY, "MERCHANT"));
            } else if (status == OrderStatus.OUT_FOR_DELIVERY) {
                btnTrackingActionPrimary.setVisibility(View.VISIBLE);
                btnTrackingActionPrimary.setEnabled(false);
                btnTrackingActionPrimary.setText("Đang giao hàng...");
            }
        } else {
            // CUSTOMER
            if (status == OrderStatus.OUT_FOR_DELIVERY) {
                btnTrackingActionPrimary.setVisibility(View.VISIBLE);
                btnTrackingActionPrimary.setText("Đã nhận được hàng");
                btnTrackingActionPrimary.setOnClickListener(v -> trackingViewModel.sendStatusUpdate(OrderStatus.DELIVERED, "CUSTOMER"));
            } else if (status == OrderStatus.ORDER_PLACED || status == OrderStatus.ORDER_PACKED) {
                btnTrackingActionPrimary.setVisibility(View.VISIBLE);
                btnTrackingActionPrimary.setText("Hủy đơn hàng");
                btnTrackingActionPrimary.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Hủy đơn hàng")
                        .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                        .setPositiveButton("Hủy đơn", (dialog, which) -> {
                            trackingViewModel.sendStatusUpdate(OrderStatus.CANCELLED, "CUSTOMER");
                            AppUtils.showToast(this, "Đang xử lý hủy đơn...");
                        })
                        .setNegativeButton("Quay lại", null)
                        .show();
                });
            }
        }
    }
}