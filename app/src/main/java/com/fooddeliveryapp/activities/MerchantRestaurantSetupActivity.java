package com.fooddeliveryapp.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.remote.dto.RestaurantUpsertRequest;
import com.fooddeliveryapp.remote.dto.UploadResponse;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.FileUtils;
import com.fooddeliveryapp.utils.NetworkUtils;
import com.fooddeliveryapp.utils.SessionManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantRestaurantSetupActivity extends AppCompatActivity {

    private ApiService apiService;

    private ImageView ivLogo;
    private TextView btnPickLogo;
    private EditText etName;
    private EditText etAddress;
    private EditText etDesc;
    private EditText etPhone;
    private TextView tvOpenTime;
    private TextView tvCloseTime;
    private TextView btnSave;

    private Uri selectedLogoUri = null;
    private String uploadedLogoUrl = null;
    private String uploadedLogoPublicId = null;
    private String openTime = null;  // HH:mm
    private String closeTime = null; // HH:mm

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_restaurant_setup);

        apiService = ApiClient.getClient(this).create(ApiService.class);

        ivLogo = findViewById(R.id.ivRestaurantLogo);
        btnPickLogo = findViewById(R.id.btnPickLogo);
        etName = findViewById(R.id.etRestaurantName);
        etAddress = findViewById(R.id.etRestaurantAddress);
        etDesc = findViewById(R.id.etRestaurantDesc);
        etPhone = findViewById(R.id.etRestaurantPhone);
        tvOpenTime = findViewById(R.id.tvOpenTime);
        tvCloseTime = findViewById(R.id.tvCloseTime);
        btnSave = findViewById(R.id.btnSaveRestaurant);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedLogoUri = uri;
                uploadedLogoUrl = null;
                uploadedLogoPublicId = null;
                ivLogo.setImageURI(uri);
            }
        });

        btnPickLogo.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        tvOpenTime.setOnClickListener(v -> pickTime(true));
        tvCloseTime.setOnClickListener(v -> pickTime(false));
        btnSave.setOnClickListener(v -> save());
    }

    private void pickTime(boolean isOpen) {
        Calendar cal = Calendar.getInstance();
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String hh = (hourOfDay < 10 ? "0" : "") + hourOfDay;
            String mm = (minute < 10 ? "0" : "") + minute;
            String val = hh + ":" + mm;
            if (isOpen) {
                openTime = val;
                tvOpenTime.setText("Mở cửa: " + val);
            } else {
                closeTime = val;
                tvCloseTime.setText("Đóng cửa: " + val);
            }
        }, h, m, true).show();
    }

    private void setEnabled(boolean enabled) {
        btnPickLogo.setEnabled(enabled);
        btnPickLogo.setAlpha(enabled ? 1f : 0.6f);
        btnSave.setEnabled(enabled);
        btnSave.setAlpha(enabled ? 1f : 0.6f);
    }

    private void save() {
        long ownerId = SessionManager.getInstance(this).getUserId();
        if (ownerId <= 0) {
            AppUtils.showToast(this, "Session expired. Please login again.");
            return;
        }

        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(openTime) || TextUtils.isEmpty(closeTime)) {
            AppUtils.showToast(this, "Vui lòng chọn giờ mở/đóng cửa");
            return;
        }

        setEnabled(false);

        if (selectedLogoUri != null && uploadedLogoUrl == null) {
            uploadLogoThenCreate(ownerId, name, address, desc, phone);
        } else {
            createRestaurant(ownerId, name, address, desc, phone, uploadedLogoUrl);
        }
    }

    private void uploadLogoThenCreate(long ownerId, String name, String address, String desc, String phone) {
        try {
            java.io.File file = FileUtils.copyUriToCacheFile(this, selectedLogoUri, "logo.jpg");
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
            RequestBody ctx = RequestBody.create(MediaType.parse("text/plain"), "restaurant");

            apiService.uploadImage(part, ctx).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful() && response.body() != null && !TextUtils.isEmpty(response.body().getUrl())) {
                        uploadedLogoUrl = response.body().getUrl();
                        uploadedLogoPublicId = response.body().getPublicId();
                        createRestaurant(ownerId, name, address, desc, phone, uploadedLogoUrl);
                    } else {
                        setEnabled(true);
                        String err = NetworkUtils.readError(response);
                        AppUtils.showToast(MerchantRestaurantSetupActivity.this,
                                err != null ? err : ("Upload failed (" + response.code() + ")"));
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    setEnabled(true);
                    AppUtils.showToast(MerchantRestaurantSetupActivity.this, "Upload failed");
                }
            });
        } catch (Exception e) {
            setEnabled(true);
            AppUtils.showToast(this, "Upload error");
        }
    }

    private void createRestaurant(long ownerId, String name, String address, String desc, String phone, String logoUrl) {
        RestaurantUpsertRequest req = new RestaurantUpsertRequest(
                ownerId,
                name,
                TextUtils.isEmpty(desc) ? null : desc,
                TextUtils.isEmpty(logoUrl) ? null : logoUrl,
                TextUtils.isEmpty(uploadedLogoPublicId) ? null : uploadedLogoPublicId,
                TextUtils.isEmpty(address) ? null : address,
                TextUtils.isEmpty(phone) ? null : phone,
                null
        );

        apiService.addRestaurant(req).enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    long restaurantId = response.body().getId();
                    upsertHoursAllDaysThenOpenDetail(restaurantId);
                } else {
                    setEnabled(true);
                    AppUtils.showToast(MerchantRestaurantSetupActivity.this, "Save failed (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                setEnabled(true);
                AppUtils.showToast(MerchantRestaurantSetupActivity.this, "Network Error");
            }
        });
    }

    private void upsertHoursAllDaysThenOpenDetail(long restaurantId) {
        Map<String, Object> body = new HashMap<>();
        body.put("openTime", openTime + ":00");
        body.put("closeTime", closeTime + ":00");
        body.put("closed", false);

        String[] days = new String[] {"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"};
        upsertDayRecursive(restaurantId, 0, days, body);
    }

    private void upsertDayRecursive(long restaurantId, int idx, String[] days, Map<String, Object> body) {
        if (idx >= days.length) {
            Intent intent = new Intent(this, MerchantRestaurantDetailActivity.class);
            intent.putExtra(MerchantRestaurantDetailActivity.EXTRA_RESTAURANT_ID, (int) restaurantId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        apiService.upsertBusinessHours(restaurantId, days[idx], body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    upsertDayRecursive(restaurantId, idx + 1, days, body);
                } else {
                    setEnabled(true);
                    AppUtils.showToast(MerchantRestaurantSetupActivity.this, "Hours save failed (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                setEnabled(true);
                AppUtils.showToast(MerchantRestaurantSetupActivity.this, "Network Error");
            }
        });
    }

    // readAllBytesFromUri removed: we now stream URI -> temp File for Retrofit multipart
}

