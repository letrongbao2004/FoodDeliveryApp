package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.fragments.CartFragment;
import com.fooddeliveryapp.fragments.HomeFragment;
import com.fooddeliveryapp.fragments.OrdersFragment;
import com.fooddeliveryapp.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_cart) {
            fragment = new CartFragment();
        } else if (id == R.id.nav_orders) {
            fragment = new OrdersFragment();
        } else if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
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
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /** Called from fragments to refresh cart badge. */
    public void updateCartBadge(int count) {
        if (count > 0) {
            bottomNav.getOrCreateBadge(R.id.nav_cart).setNumber(count);
        } else {
            bottomNav.removeBadge(R.id.nav_cart);
        }
    }

    /** Navigate directly to cart tab. */
    public void openCart() {
        bottomNav.setSelectedItemId(R.id.nav_cart);
    }
}