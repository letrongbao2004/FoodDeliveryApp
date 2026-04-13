package com.fooddeliveryapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.OrderAdapter;
import com.fooddeliveryapp.managers.OrderManager;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.utils.AppUtils;

import java.util.List;

public class MerchantOrderHistoryActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private static final String EXTRA_RESTAURANT_ID = "restaurant_id";

    public static Intent newIntent(Context context, int restaurantId) {
        Intent i = new Intent(context, MerchantOrderHistoryActivity.class);
        i.putExtra(EXTRA_RESTAURANT_ID, restaurantId);
        return i;
    }

    private int restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_order_history);

        restaurantId = getIntent().getIntExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId <= 0) {
            finish();
            return;
        }

        RecyclerView rv = findViewById(R.id.rvMerchantOrders);
        rv.setLayoutManager(new LinearLayoutManager(this));

        OrderManager.getInstance(this).getRestaurantOrders(restaurantId, new OrderManager.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                rv.setAdapter(new OrderAdapter(MerchantOrderHistoryActivity.this, orders, MerchantOrderHistoryActivity.this));
            }

            @Override
            public void onError(String message) {
                AppUtils.showToast(MerchantOrderHistoryActivity.this, "Failed to load orders");
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    @Override
    public void onTrackOrder(Order order) {
        onOrderClick(order);
    }
}

