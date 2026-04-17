package com.fooddeliveryapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.AdCarouselAdapter;
import com.fooddeliveryapp.models.Advertisement;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.AppUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantDashboardActivity extends AppCompatActivity {

    private static final String EXTRA_RESTAURANT_ID = "restaurant_id";
    private AdCarouselAdapter adAdapter;
    private ViewPager2 vpAds;
    private TabLayout tabAds;
    private ApiService apiService;
    private int restaurantId;

    public static Intent newIntent(Context context, int restaurantId) {
        Intent i = new Intent(context, MerchantDashboardActivity.class);
        i.putExtra(EXTRA_RESTAURANT_ID, restaurantId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_dashboard);
        restaurantId = getIntent().getIntExtra(EXTRA_RESTAURANT_ID, -1);
        apiService = ApiClient.getClient(this).create(ApiService.class);

        // TODO: replace with backend stats endpoint.
        TextView tvRevenue = findViewById(R.id.tvDashRevenue);
        TextView tvTotal = findViewById(R.id.tvDashTotalOrders);
        TextView tvNew = findViewById(R.id.tvDashNewOrders);
        vpAds = findViewById(R.id.vpMerchantAds);
        tabAds = findViewById(R.id.tabMerchantAdsIndicator);

        tvRevenue.setText(AppUtils.formatPrice(0));
        tvTotal.setText("0");
        tvNew.setText("0");

        adAdapter = new AdCarouselAdapter(this, true, new AdCarouselAdapter.Listener() {
            @Override
            public void onBannerClick(Advertisement ad) {
                openMerchantMenu();
            }

            @Override
            public void onPrimaryCtaClick(Advertisement ad) {
                openMerchantMenu();
            }

            @Override
            public void onSecondaryCtaClick(Advertisement ad) {
                // Merchant has no purchase action.
            }
        });
        if (vpAds != null) {
            vpAds.setAdapter(adAdapter);
            if (tabAds != null) {
                new TabLayoutMediator(tabAds, vpAds, (tab, position) -> {}).attach();
            }
            loadAds();
        }
    }

    private void loadAds() {
        apiService.getAds(5).enqueue(new Callback<List<Advertisement>>() {
            @Override
            public void onResponse(Call<List<Advertisement>> call, Response<List<Advertisement>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                adAdapter.submit(response.body());
                if (vpAds != null) {
                    vpAds.setVisibility(response.body().isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
                }
                if (tabAds != null) {
                    tabAds.setVisibility(response.body().size() > 1 ? android.view.View.VISIBLE : android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Advertisement>> call, Throwable t) {
                if (vpAds != null) vpAds.setVisibility(android.view.View.GONE);
                if (tabAds != null) tabAds.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void openMerchantMenu() {
        if (restaurantId <= 0) {
            Toast.makeText(this, "Restaurant not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, MerchantRestaurantDetailActivity.class);
        intent.putExtra(MerchantRestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }
}

