package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.AdCarouselAdapter;
import com.fooddeliveryapp.adapters.FoodAdapter;
import com.fooddeliveryapp.dialogs.FoodCustomizationDialog;
import com.fooddeliveryapp.managers.CartManager;
import com.fooddeliveryapp.models.Advertisement;
import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class RestaurantDetailActivity extends AppCompatActivity
        implements FoodAdapter.OnFoodClickListener {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";

    private ApiService apiService;
    private CartManager cartManager;
    private SessionManager session;
    private Restaurant restaurant;
    private int restaurantId;

    private TextView tvName, tvCategory, tvRating, tvDeliveryTime,
            tvDeliveryFee, tvDistance, tvStatus;
    private android.widget.ImageView ivRestaurantHeader;
    private RecyclerView rvFoods;
    private FoodAdapter foodAdapter;
    private ViewPager2 vpAds;
    private TabLayout tabAds;
    private AdCarouselAdapter adAdapter;
    private final List<Food> foodCache = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        cartManager = CartManager.getInstance(this);
        session = SessionManager.getInstance(this);

        restaurantId = getIntent().getIntExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId == -1) {
            finish();
            return;
        }

        bindViews();
        setupAdsCarousel();
        fetchRestaurantDetails();
    }

    private void bindViews() {
        tvName = findViewById(R.id.tvRestaurantDetailName);
        tvCategory = findViewById(R.id.tvRestaurantDetailCategory);
        tvRating = findViewById(R.id.tvRestaurantDetailRating);
        tvDeliveryTime = findViewById(R.id.tvRestaurantDetailDeliveryTime);
        tvDeliveryFee = findViewById(R.id.tvRestaurantDetailDeliveryFee);
        tvDistance = findViewById(R.id.tvRestaurantDetailDistance);
        tvStatus = findViewById(R.id.tvRestaurantDetailStatus);
        ivRestaurantHeader = findViewById(R.id.ivRestaurantHeader);
        rvFoods = findViewById(R.id.rvFoods);
        vpAds = findViewById(R.id.vpRestaurantAdsInline);
        tabAds = findViewById(R.id.tabRestaurantAdsInlineIndicator);
    }

    private void setupAdsCarousel() {
        adAdapter = new AdCarouselAdapter(this, false, new AdCarouselAdapter.Listener() {
            @Override
            public void onBannerClick(Advertisement ad) {
                openAdFood(ad);
            }

            @Override
            public void onPrimaryCtaClick(Advertisement ad) {
                addAdFoodToCart(ad);
            }

            @Override
            public void onSecondaryCtaClick(Advertisement ad) {
                openAdFood(ad);
            }
        });
        if (vpAds != null) {
            vpAds.setAdapter(adAdapter);
            if (tabAds != null) {
                new TabLayoutMediator(tabAds, vpAds, (tab, position) -> {}).attach();
            }
        }
    }

    private void fetchRestaurantDetails() {
        // Fetch all restaurants and find ours (as a simple fallback)
        apiService.getRestaurants().enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (isFinishing() || isDestroyed())
                    return;
                if (response.isSuccessful() && response.body() != null) {
                    for (Restaurant r : response.body()) {
                        if (r.getId() == restaurantId) {
                            restaurant = r;
                            break;
                        }
                    }
                    if (restaurant != null) {
                        setupToolbar();
                        populateHeader();
                        loadAds();
                        loadFoods();
                    } else {
                        Toast.makeText(RestaurantDetailActivity.this, "Restaurant not found", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                if (isFinishing() || isDestroyed())
                    return;
                Toast.makeText(RestaurantDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(restaurant.getName());
        }
    }

    private void populateHeader() {
        tvName.setText(restaurant.getName());
        tvCategory.setText(restaurant.getCategory());
        tvRating.setText(restaurant.getRatingText() + " ★");
        tvDeliveryTime.setText(restaurant.getDeliveryTimeText());
        tvDeliveryFee.setText(restaurant.getDeliveryFee() == 0 ? "Free Delivery"
                : AppUtils.formatPrice(restaurant.getDeliveryFee()) + " delivery");
        tvDistance.setText(restaurant.getDistanceText());
        tvStatus.setText(restaurant.isOpen() ? "● Open" : "● Closed");
        tvStatus.setTextColor(getColor(restaurant.isOpen() ? R.color.success : R.color.danger));
        if (restaurant.getImageUrl() != null && !restaurant.getImageUrl().isEmpty()) {
            Glide.with(this).load(restaurant.getImageUrl()).centerCrop().into(ivRestaurantHeader);
        } else {
            ivRestaurantHeader.setImageResource(R.mipmap.ic_launcher);
        }
    }

    private void loadFoods() {
        // Show/hide closed banner
        View closedBanner = findViewById(R.id.viewClosedBanner);
        if (closedBanner != null) {
            closedBanner.setVisibility(restaurant.isOpen() ? View.GONE : View.VISIBLE);
        }

        apiService.getFoods(restaurant.getId()).enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (isFinishing() || isDestroyed())
                    return;
                if (response.isSuccessful() && response.body() != null) {
                    foodCache.clear();
                    foodCache.addAll(response.body());
                    // Pass restaurant open status — adapter disables ordering when closed
                    foodAdapter = new FoodAdapter(
                            RestaurantDetailActivity.this,
                            foodCache,
                            RestaurantDetailActivity.this,
                            restaurant.isOpen());
                    rvFoods.setLayoutManager(new LinearLayoutManager(RestaurantDetailActivity.this));
                    rvFoods.setAdapter(foodAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                if (isFinishing() || isDestroyed())
                    return;
                Toast.makeText(RestaurantDetailActivity.this, "Failed to load menu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAds() {
        apiService.getAds(10).enqueue(new Callback<List<Advertisement>>() {
            @Override
            public void onResponse(Call<List<Advertisement>> call, Response<List<Advertisement>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) return;
                List<Advertisement> filtered = new ArrayList<>();
                for (Advertisement ad : response.body()) {
                    if (ad.getMenuItem() != null && ad.getMenuItem().getRestaurantId() == restaurantId) {
                        filtered.add(ad);
                    }
                }
                adAdapter.submit(filtered);
                if (vpAds != null) {
                    vpAds.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
                }
                if (tabAds != null) {
                    tabAds.setVisibility(filtered.size() > 1 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Advertisement>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (vpAds != null) vpAds.setVisibility(View.GONE);
                if (tabAds != null) tabAds.setVisibility(View.GONE);
            }
        });
    }

    private void openAdFood(Advertisement ad) {
        Food food = findFoodByAd(ad);
        if (food != null) {
            showCustomizationDialog(food);
        }
    }

    private void addAdFoodToCart(Advertisement ad) {
        Food food = findFoodByAd(ad);
        if (food == null) {
            Toast.makeText(this, "Menu item not found", Toast.LENGTH_SHORT).show();
            return;
        }
        onAddToCartClick(food);
    }

    private Food findFoodByAd(Advertisement ad) {
        if (ad == null || ad.getMenuItem() == null) return null;
        int targetId = ad.getMenuItem().getId();
        for (Food f : foodCache) {
            if (f.getId() == targetId) return f;
        }
        return null;
    }

    @Override
    public void onFoodClick(Food food) {
        showCustomizationDialog(food);
    }

    @Override
    public void onAddToCartClick(Food food) {
        cartManager.addToCart(session.getUserId(), food, 1, "Regular", "Normal", "", "");

        com.google.android.material.snackbar.Snackbar.make(
                findViewById(android.R.id.content),
                String.format("Đã thêm 1x %s vào giỏ hàng!", food.getName()),
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG).setAction("GIỎ HÀNG", v -> {
                    startActivity(new Intent(RestaurantDetailActivity.this, CartActivity.class));
                }).show();
    }

    private void showCustomizationDialog(Food food) {
        FoodCustomizationDialog dialog = FoodCustomizationDialog.newInstance(food);
        dialog.setOnAddToCartListener((f, qty, size, spice, addOns, notes) -> {
            cartManager.addToCart(session.getUserId(), f, qty, size, spice, addOns, notes);

            com.google.android.material.snackbar.Snackbar.make(
                    findViewById(android.R.id.content),
                    String.format("Đã thêm %dx %s vào giỏ hàng!", qty, f.getName()),
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG).setAction("GIỎ HÀNG", v -> {
                        startActivity(new Intent(RestaurantDetailActivity.this, CartActivity.class));
                    }).show();
        });
        dialog.show(getSupportFragmentManager(), "FoodCustomization");
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