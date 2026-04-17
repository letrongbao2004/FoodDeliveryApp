package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.FoodAdapter;
import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.remote.dto.UploadResponse;
import com.fooddeliveryapp.remote.dto.FoodUpsertRequest;
import com.fooddeliveryapp.remote.dto.RestaurantUpsertRequest;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.FileUtils;
import com.fooddeliveryapp.utils.NetworkUtils;
import com.fooddeliveryapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MerchantRestaurantDetailActivity extends AppCompatActivity implements FoodAdapter.OnFoodClickListener {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";

    private ApiService apiService;
    private int restaurantId;

    private TextView tvName;
    private TextView tvCategory;
    private TextView tvMeta;
    private TextView btnDashboard;
    private TextView btnOrders;
    private TextView btnChat;
    private TextView btnEditRestaurant;
    private TextView btnMerchantLogout;
    private View btnAddFood;
    private RecyclerView rvFoods;
    private ImageView ivRestaurantHeader;

    private FoodAdapter adapter;
    private final List<Food> foods = new ArrayList<>();

    private Restaurant restaurant;

    private ActivityResultLauncher<String> pickFoodImageLauncher;
    private Uri selectedFoodImageUri = null;
    private ImageView currentFoodImagePreview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_restaurant_detail);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        restaurantId = getIntent().getIntExtra(EXTRA_RESTAURANT_ID, -1);
        if (restaurantId <= 0) {
            finish();
            return;
        }

        bindViews();
        setupToolbar();
        initPickers();
        fetchRestaurantDetails();
    }

    private void initPickers() {
        pickFoodImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedFoodImageUri = uri;
                if (currentFoodImagePreview != null) {
                    currentFoodImagePreview.setImageURI(uri);
                }
            }
        });
    }

    private void bindViews() {
        tvName = findViewById(R.id.tvRestaurantDetailName);
        tvCategory = findViewById(R.id.tvRestaurantDetailCategory);
        tvMeta = findViewById(R.id.tvMerchantMeta);
        btnDashboard = findViewById(R.id.btnMerchantDashboard);
        btnOrders = findViewById(R.id.btnMerchantOrders);
        btnChat = findViewById(R.id.btnMerchantChat);
        btnEditRestaurant = findViewById(R.id.btnEditRestaurant);
        btnMerchantLogout = findViewById(R.id.btnMerchantLogout);
        btnAddFood = findViewById(R.id.btnAddFood);
        rvFoods = findViewById(R.id.rvMerchantMenuFoods);
        ivRestaurantHeader = findViewById(R.id.ivRestaurantHeader);

        adapter = new FoodAdapter(this, foods, this, true, true);
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(adapter);

        btnAddFood.setOnClickListener(v -> showAddFoodDialog());
        btnDashboard.setOnClickListener(v -> startActivity(MerchantDashboardActivity.newIntent(this, restaurantId)));
        btnOrders.setOnClickListener(v -> startActivity(MerchantOrderHistoryActivity.newIntent(this, restaurantId)));
        btnEditRestaurant.setOnClickListener(v -> showEditRestaurantDialog());
        btnMerchantLogout.setOnClickListener(v -> logoutMerchant());

        btnChat.setOnClickListener(v -> startActivity(new Intent(this, MerchantChatListActivity.class)));
    }

    private void showEditRestaurantDialog() {
        if (restaurant == null) return;
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_restaurant, null);
        EditText etName = dialogView.findViewById(R.id.etEditRestaurantName);
        EditText etCategory = dialogView.findViewById(R.id.etEditRestaurantCategory);
        EditText etDesc = dialogView.findViewById(R.id.etEditRestaurantDesc);
        EditText etAddress = dialogView.findViewById(R.id.etEditRestaurantAddress);
        EditText etPhone = dialogView.findViewById(R.id.etEditRestaurantPhone);

        etName.setText(restaurant.getName());
        etCategory.setText(restaurant.getCategory());
        etDesc.setText(restaurant.getDescription());
        etAddress.setText(restaurant.getAddress());
        etPhone.setText(restaurant.getPhone());

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Edit restaurant")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Required");
                return;
            }

            RestaurantUpsertRequest req = new RestaurantUpsertRequest(
                    restaurant.getOwnerId(),
                    name,
                    TextUtils.isEmpty(desc) ? null : desc,
                    TextUtils.isEmpty(restaurant.getImageUrl()) ? null : restaurant.getImageUrl(),
                    null,
                    TextUtils.isEmpty(address) ? null : address,
                    TextUtils.isEmpty(phone) ? null : phone,
                    TextUtils.isEmpty(category) ? null : category
            );

            apiService.updateRestaurant(restaurantId, req).enqueue(new Callback<Restaurant>() {
                @Override
                public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        restaurant = response.body();
                        renderRestaurant();
                        dialog.dismiss();
                        AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Restaurant updated");
                    } else {
                        String err = NetworkUtils.readError(response);
                        AppUtils.showToast(MerchantRestaurantDetailActivity.this,
                                err != null ? err : ("Update failed (" + response.code() + ")"));
                    }
                }

                @Override
                public void onFailure(Call<Restaurant> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Network Error");
                }
            });
        }));
        dialog.show();
    }

    private void logoutMerchant() {
        SessionManager.getInstance(this).logout();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Restaurant detail");
        }
    }

    private void fetchRestaurantDetails() {
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
                    if (restaurant == null) {
                        Toast.makeText(MerchantRestaurantDetailActivity.this, "Restaurant not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    renderRestaurant();
                } else {
                    Toast.makeText(MerchantRestaurantDetailActivity.this, "Failed to load restaurant", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                if (isFinishing() || isDestroyed())
                    return;
                Toast.makeText(MerchantRestaurantDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void renderRestaurant() {
        tvName.setText(restaurant.getName());
        tvCategory.setText(TextUtils.isEmpty(restaurant.getCategory()) ? "" : restaurant.getCategory());

        StringBuilder meta = new StringBuilder();
        if (!TextUtils.isEmpty(restaurant.getDescription())) meta.append(restaurant.getDescription()).append('\n');
        if (!TextUtils.isEmpty(restaurant.getAddress())) meta.append("Địa chỉ: ").append(restaurant.getAddress()).append('\n');
        if (!TextUtils.isEmpty(restaurant.getPhone())) meta.append("SĐT: ").append(restaurant.getPhone()).append('\n');
        if (!TextUtils.isEmpty(restaurant.getOpenHours())) meta.append("Giờ: ").append(restaurant.getOpenHours()).append('\n');
        tvMeta.setText(meta.toString().trim());
        if (!TextUtils.isEmpty(restaurant.getImageUrl())) {
            Glide.with(this).load(restaurant.getImageUrl()).centerCrop().into(ivRestaurantHeader);
        } else {
            ivRestaurantHeader.setImageResource(R.mipmap.ic_launcher);
        }

        loadFoods();
    }

    private void loadFoods() {
        apiService.getFoods(restaurantId).enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    foods.clear();
                    foods.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(MerchantRestaurantDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddFoodDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        EditText etName = dialogView.findViewById(R.id.etAddFoodName);
        ImageView ivFoodImage = dialogView.findViewById(R.id.ivAddFoodImage);
        TextView btnPickFoodImage = dialogView.findViewById(R.id.btnPickFoodImage);
        EditText etDesc = dialogView.findViewById(R.id.etAddFoodDesc);
        EditText etPrice = dialogView.findViewById(R.id.etAddFoodPrice);
        EditText etCat = dialogView.findViewById(R.id.etAddFoodCategory);

        selectedFoodImageUri = null;
        currentFoodImagePreview = ivFoodImage;
        btnPickFoodImage.setOnClickListener(v -> pickFoodImageLauncher.launch("image/*"));

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Add New Food")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String cat = etCat.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Required");
                return;
            }
            if (TextUtils.isEmpty(priceStr)) {
                etPrice.setError("Required");
                return;
            }
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid number");
                return;
            }
            if (price <= 0) {
                etPrice.setError("Must be > 0");
                return;
            }

            if (selectedFoodImageUri != null) {
                uploadFoodImageThenCreate(dialog, restaurantId, name, desc, price, cat);
            } else {
                createFood(dialog, restaurantId, name, desc, price, null, null, cat);
            }
        }));

        dialog.show();
    }

    private void uploadFoodImageThenCreate(android.app.AlertDialog dialog, int restaurantId,
                                          String name, String desc, double price, String cat) {
        try {
            java.io.File file = FileUtils.copyUriToCacheFile(this, selectedFoodImageUri, "food.jpg");
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
            RequestBody ctx = RequestBody.create(MediaType.parse("text/plain"), "menu_item");

            apiService.uploadImage(part, ctx).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful() && response.body() != null && !TextUtils.isEmpty(response.body().getUrl())) {
                        createFood(dialog, restaurantId, name, desc, price,
                                response.body().getUrl(), response.body().getPublicId(), cat);
                    } else {
                        String err = NetworkUtils.readError(response);
                        AppUtils.showToast(MerchantRestaurantDetailActivity.this,
                                err != null ? err : ("Image upload failed (" + response.code() + ")"));
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Image upload failed");
                }
            });
        } catch (Exception e) {
            AppUtils.showToast(this, "Image upload error");
        }
    }

    private void createFood(android.app.AlertDialog dialog, int restaurantId, String name, String desc,
                            double price, String imageUrl, String imagePublicId, String cat) {
        FoodUpsertRequest req = new FoodUpsertRequest(
                restaurantId,
                name,
                TextUtils.isEmpty(desc) ? null : desc,
                price,
                TextUtils.isEmpty(imageUrl) ? null : imageUrl,
                TextUtils.isEmpty(imagePublicId) ? null : imagePublicId,
                TextUtils.isEmpty(cat) ? null : cat
        );

        apiService.addFood(req).enqueue(new Callback<Food>() {
            @Override
            public void onResponse(Call<Food> call, Response<Food> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    foods.add(0, response.body());
                    adapter.notifyItemInserted(0);
                    dialog.dismiss();
                    AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Added");
                } else {
                    AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Add failed (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Food> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Network Error");
            }
        });
    }

    @Override
    public void onFoodClick(Food food) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(food.getName())
                .setMessage("Delete this food?")
                .setPositiveButton("Delete", (d, w) -> deleteFood(food))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAddToCartClick(Food food) {
        // Not used in merchant mode
    }

    private void deleteFood(Food food) {
        long id = food.getId();
        apiService.deleteFood(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    int idx = foods.indexOf(food);
                    if (idx >= 0) {
                        foods.remove(idx);
                        adapter.notifyItemRemoved(idx);
                    } else {
                        loadFoods();
                    }
                    AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Deleted");
                } else {
                    AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Delete failed (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                AppUtils.showToast(MerchantRestaurantDetailActivity.this, "Network Error");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

