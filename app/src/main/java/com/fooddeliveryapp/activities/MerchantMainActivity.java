package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.fragments.merchant.MerchantDashboardFragment;
import com.fooddeliveryapp.fragments.merchant.MerchantMenuFragment;
import com.fooddeliveryapp.fragments.merchant.MerchantOrdersFragment;
import com.fooddeliveryapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MerchantMainActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNav;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_main);

        session = SessionManager.getInstance(this);
        bottomNav = findViewById(R.id.merchantBottomNav);
        bottomNav.setOnItemSelectedListener(this);

        TextView tvMerchantGreeting = findViewById(R.id.tvMerchantGreeting);
        tvMerchantGreeting.setText("Welcome, " + session.getName());

        if (savedInstanceState == null) {
            loadFragment(new MerchantDashboardFragment());
        }

        // Logout
        TextView btnMerchantLogout = findViewById(R.id.btnMerchantLogout);
        if (btnMerchantLogout != null) {
            btnMerchantLogout.setOnClickListener(v -> {
                session.logout();
                Intent intent = new Intent(this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();
        if (id == R.id.merchant_nav_dashboard) {
            fragment = new MerchantDashboardFragment();
        } else if (id == R.id.merchant_nav_orders) {
            fragment = new MerchantOrdersFragment();
        } else if (id == R.id.merchant_nav_menu) {
            fragment = new MerchantMenuFragment();
        }
        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.merchantFragmentContainer, fragment)
                .commit();
    }
}