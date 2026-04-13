package com.fooddeliveryapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.utils.AppUtils;

public class MerchantDashboardActivity extends AppCompatActivity {

    private static final String EXTRA_RESTAURANT_ID = "restaurant_id";

    public static Intent newIntent(Context context, int restaurantId) {
        Intent i = new Intent(context, MerchantDashboardActivity.class);
        i.putExtra(EXTRA_RESTAURANT_ID, restaurantId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_dashboard);

        // TODO: replace with backend stats endpoint.
        TextView tvRevenue = findViewById(R.id.tvDashRevenue);
        TextView tvTotal = findViewById(R.id.tvDashTotalOrders);
        TextView tvNew = findViewById(R.id.tvDashNewOrders);

        tvRevenue.setText(AppUtils.formatPrice(0));
        tvTotal.setText("0");
        tvNew.setText("0");
    }
}

