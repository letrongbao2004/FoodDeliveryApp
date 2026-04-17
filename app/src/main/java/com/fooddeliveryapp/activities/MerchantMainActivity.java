package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantMainActivity extends AppCompatActivity {

    private SessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = SessionManager.getInstance(this);
        apiService = ApiClient.getClient(this).create(ApiService.class);

        routeMerchant();
    }

    private void routeMerchant() {
        long ownerId = session.getUserId();
        if (ownerId <= 0) {
            session.logout();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        apiService.getRestaurantByOwner(ownerId).enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(MerchantMainActivity.this, MerchantRestaurantDetailActivity.class);
                    intent.putExtra(MerchantRestaurantDetailActivity.EXTRA_RESTAURANT_ID, response.body().getId());
                    startActivity(intent);
                    finish();
                    return;
                }

                // 404: no restaurant yet -> show single create screen
                showNoRestaurantScreen();
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showNoRestaurantScreen();
            }
        });
    }

    private void showNoRestaurantScreen() {
        setContentView(R.layout.activity_merchant_no_restaurant);
        TextView btnCreate = findViewById(R.id.btnCreateRestaurant);
        TextView btnLogout = findViewById(R.id.btnMerchantLogoutNoRestaurant);

        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(MerchantMainActivity.this, MerchantRestaurantSetupActivity.class)));

        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(MerchantMainActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}