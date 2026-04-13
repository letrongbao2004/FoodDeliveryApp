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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    private TextView tvOrderCode, tvStatus, tvDate, tvSubtotal, tvDeliveryFee, tvTotal;
    private RecyclerView rvItems;
    private ProgressBar progressBar;

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
        progressBar = new ProgressBar(this); // fallback — not in this layout

        long orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1L);
        if (orderId == -1L) {
            orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
            if (orderId == -1) {
                finish();
                return;
            }
        }

        loadOrderDetail(orderId);
    }

    private void loadOrderDetail(long orderId) {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getOrderDetail(orderId).enqueue(new Callback<OrderDetail>() {
            @Override
            public void onResponse(Call<OrderDetail> call, Response<OrderDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindDetail(response.body());
                } else {
                    AppUtils.showToast(OrderDetailActivity.this, "Could not load order detail");
                }
            }

            @Override
            public void onFailure(Call<OrderDetail> call, Throwable t) {
                AppUtils.showToast(OrderDetailActivity.this, "Network error: " + t.getMessage());
            }
        });
    }

    private void bindDetail(OrderDetail detail) {
        tvOrderCode.setText(detail.getOrderCode());
        tvStatus.setText(detail.getStatus());

        // Status background color
        int statusColor = AppUtils.getStatusColor(detail.getStatus());
        tvStatus.setBackgroundColor(getColor(statusColor));

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
}