package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.AdCarouselAdapter;
import com.fooddeliveryapp.adapters.FoodAdapter;
import com.fooddeliveryapp.dialogs.MerchantDialogHelper;
import com.fooddeliveryapp.models.Advertisement;
import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.remote.dto.AdUpsertRequest;
import com.fooddeliveryapp.remote.dto.FoodUpsertRequest;
import com.fooddeliveryapp.remote.dto.RestaurantUpsertRequest;
import com.fooddeliveryapp.remote.dto.UploadResponse;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.FileUtils;
import com.fooddeliveryapp.utils.NetworkUtils;
import com.fooddeliveryapp.utils.SessionManager;
import com.fooddeliveryapp.viewmodels.MerchantRestaurantViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantRestaurantDetailActivity extends AppCompatActivity implements FoodAdapter.OnFoodClickListener {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";

    private MerchantRestaurantViewModel viewModel;
    private ApiService apiService;
    private int restaurantId;

    private TextView tvName, tvCategory, tvMeta;
    private ImageView ivHeader;
    private FoodAdapter foodAdapter;
    private AdCarouselAdapter adAdapter;
    private ViewPager2 vpAds;
    private TabLayout tabAds;

    private ActivityResultLauncher<String> imagePicker;
    private Uri selectedUri;
    private ImageView currentPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_restaurant_detail);

        restaurantId = getIntent().getIntExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId <= 0) { finish(); return; }

        apiService = ApiClient.getClient(this).create(ApiService.class);
        viewModel = new ViewModelProvider(this).get(MerchantRestaurantViewModel.class);

        setupUI();
        observeViewModel();
        
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedUri = uri;
                if (currentPreview != null) currentPreview.setImageURI(uri);
            }
        });

        viewModel.fetchRestaurantDetails(apiService, restaurantId);
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Restaurant Manager");
        }

        tvName = findViewById(R.id.tvRestaurantDetailName);
        tvCategory = findViewById(R.id.tvRestaurantDetailCategory);
        tvMeta = findViewById(R.id.tvMerchantMeta);
        ivHeader = findViewById(R.id.ivRestaurantHeader);
        vpAds = findViewById(R.id.vpMerchantAdsInline);
        tabAds = findViewById(R.id.tabMerchantAdsInlineIndicator);

        RecyclerView rv = findViewById(R.id.rvMerchantMenuFoods);
        foodAdapter = new FoodAdapter(this, new ArrayList<>(), this, true, true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(foodAdapter);

        adAdapter = new AdCarouselAdapter(this, true, null);
        if (vpAds != null) {
            vpAds.setAdapter(adAdapter);
            new TabLayoutMediator(tabAds, vpAds, (tab, pos) -> {}).attach();
        }

        findViewById(R.id.btnMerchantDashboard).setOnClickListener(v -> startActivity(MerchantDashboardActivity.newIntent(this, restaurantId)));
        findViewById(R.id.btnMerchantOrders).setOnClickListener(v -> startActivity(MerchantOrderHistoryActivity.newIntent(this, restaurantId)));
        findViewById(R.id.btnMerchantChat).setOnClickListener(v -> startActivity(new Intent(this, MerchantChatListActivity.class)));
        findViewById(R.id.btnMerchantLogout).setOnClickListener(v -> logout());
        
        findViewById(R.id.btnEditRestaurant).setOnClickListener(v -> {
            Restaurant r = viewModel.getRestaurant().getValue();
            if (r != null) MerchantDialogHelper.showEditRestaurantDialog(this, r, this::updateRestaurant);
        });

        findViewById(R.id.btnAddFood).setOnClickListener(v -> MerchantDialogHelper.showAddFoodDialog(this, 
            view -> { currentPreview = (ImageView) view.getTag(); imagePicker.launch("image/*"); }, 
            this::handleAddFood));

        findViewById(R.id.btnAddAd).setOnClickListener(v -> {
            List<Food> foods = viewModel.getFoods().getValue();
            if (foods != null && !foods.isEmpty()) {
                MerchantDialogHelper.showAddAdDialog(this, foods,
                    view -> { currentPreview = (ImageView) view.getTag(); imagePicker.launch("image/*"); },
                    this::handleAddAd);
            } else {
                Toast.makeText(this, "Add food first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getRestaurant().observe(this, r -> {
            tvName.setText(r.getName());
            tvCategory.setText(r.getCategory());
            String meta = (r.getDescription() != null ? r.getDescription() + "\n" : "") +
                         (r.getAddress() != null ? "Địa chỉ: " + r.getAddress() + "\n" : "") +
                         (r.getPhone() != null ? "SĐT: " + r.getPhone() : "");
            tvMeta.setText(meta.trim());
            if (r.getImageUrl() != null) Glide.with(this).load(r.getImageUrl()).centerCrop().into(ivHeader);
        });

        viewModel.getFoods().observe(this, list -> {
            foodAdapter.setFoods(list);
            foodAdapter.notifyDataSetChanged();
        });

        viewModel.getAds().observe(this, list -> {
            adAdapter.submit(list);
            vpAds.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
            tabAds.setVisibility(list.size() > 1 ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateRestaurant(Restaurant r) {
        RestaurantUpsertRequest req = new RestaurantUpsertRequest(r.getOwnerId(), r.getName(), r.getDescription(), r.getImageUrl(), null, r.getAddress(), r.getPhone(), r.getCategory());
        apiService.updateRestaurant(restaurantId, req).enqueue(new Callback<Restaurant>() {
            @Override public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Cập nhật nhà hàng thành công", Toast.LENGTH_SHORT).show();
                    viewModel.refresh(apiService, restaurantId);
                } else {
                    String error = AppUtils.parseErrorBody(response);
                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Update failed: " + error, Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Restaurant> call, Throwable t) { 
                Toast.makeText(MerchantRestaurantDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show(); 
            }
        });
    }

    private void handleAddFood(MerchantDialogHelper.FoodData data) {
        if (selectedUri != null) {
            uploadImage(selectedUri, "menu_item", url -> createFood(data, url));
        } else {
            createFood(data, null);
        }
    }

    private void createFood(MerchantDialogHelper.FoodData d, String url) {
        FoodUpsertRequest req = new FoodUpsertRequest(restaurantId, d.name, d.desc, d.price, url, null, d.cat);
        req.stockQuantity = d.stockQuantity;
        apiService.addFood(req).enqueue(new Callback<Food>() {
            @Override public void onResponse(Call<Food> call, Response<Food> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Thêm món thành công", Toast.LENGTH_SHORT).show();
                    viewModel.refresh(apiService, restaurantId);
                } else {
                    String error = AppUtils.parseErrorBody(response);
                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<Food> call, Throwable t) {
                Toast.makeText(MerchantRestaurantDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleAddAd(MerchantDialogHelper.AdData data) {
        if (selectedUri != null) {
            uploadImage(selectedUri, "menu_item", url -> createAd(data, url));
        } else {
            createAd(data, data.food.getImageUrl());
        }
    }

    private void createAd(MerchantDialogHelper.AdData d, String url) {
        LocalDateTime start = LocalDateTime.now().minusMinutes(1);
        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String endStr = start.plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        AdUpsertRequest req = new AdUpsertRequest(url, d.title, d.desc, d.food.getId(), startStr, endStr);
        apiService.createAd(req).enqueue(new Callback<Advertisement>() {
            @Override public void onResponse(Call<Advertisement> call, Response<Advertisement> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Tạo quảng cáo thành công", Toast.LENGTH_SHORT).show();
                    viewModel.refresh(apiService, restaurantId);
                } else {
                    String error = AppUtils.parseErrorBody(response);
                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Ad creation failed: " + error, Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Advertisement> call, Throwable t) {
                Toast.makeText(MerchantRestaurantDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImage(Uri uri, String context, OnImageUploadedListener listener) {
        try {
            java.io.File file = FileUtils.copyUriToCacheFile(this, uri, "upload.jpg");
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);
            apiService.uploadImage(part, RequestBody.create(MediaType.parse("text/plain"), context)).enqueue(new Callback<UploadResponse>() {
                @Override public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        listener.onUploaded(response.body().getUrl());
                    } else {
                        String error = AppUtils.parseErrorBody(response);
                        Toast.makeText(MerchantRestaurantDetailActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Upload network error", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void logout() {
        SessionManager.getInstance(this).logout();
        startActivity(new Intent(this, AuthActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override public void onFoodClick(Food food) {
        new android.app.AlertDialog.Builder(this)
            .setTitle(food.getName())
            .setItems(new String[]{"Thêm vào kho (Restock)", "Xóa món (Delete)"}, (dialog, which) -> {
                if (which == 0) {
                    MerchantDialogHelper.showRestockDialog(this, food, amount -> {
                        apiService.restockFood(food.getId(), amount).enqueue(new Callback<Food>() {
                            @Override public void onResponse(Call<Food> call, Response<Food> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Cập nhật kho thành công", Toast.LENGTH_SHORT).show();
                                    viewModel.refresh(apiService, restaurantId);
                                } else {
                                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Lỗi: " + AppUtils.parseErrorBody(response), Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onFailure(Call<Food> call, Throwable t) {
                                Toast.makeText(MerchantRestaurantDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else if (which == 1) {
                    new android.app.AlertDialog.Builder(this).setTitle("Delete food?").setPositiveButton("Delete", (d, w) -> {
                        apiService.deleteFood(food.getId()).enqueue(new Callback<Void>() {
                            @Override public void onResponse(Call<Void> call, Response<Void> response) { 
                                if (response.isSuccessful()) {
                                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Xóa món thành công", Toast.LENGTH_SHORT).show();
                                    viewModel.refresh(apiService, restaurantId);
                                } else {
                                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Error: " + AppUtils.parseErrorBody(response), Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(MerchantRestaurantDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).setNegativeButton("Cancel", null).show();
                }
            }).show();
    }

    @Override public void onAddToCartClick(Food food) {}
    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    interface OnImageUploadedListener { void onUploaded(String url); }
}
