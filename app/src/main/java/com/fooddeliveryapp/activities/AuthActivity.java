package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.remote.dto.RegisterRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.Map;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.SessionManager;

public class AuthActivity extends AppCompatActivity {

    private ApiService apiService;
    private SessionManager session;

    // Views – Login
    private View layoutLogin, layoutRegister;
    private EditText etLoginEmail, etLoginPassword;
    private Button btnLogin;
    private TextView tvGoRegister, tvMerchantLogin;

    // Views – Register
    private EditText etRegName, etRegEmail, etRegPhone, etRegPassword, etRegConfirmPassword;
    private android.widget.RadioGroup rgRole;
    private Button btnRegister;
    private TextView tvGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        session = SessionManager.getInstance(this);

        bindViewsLogin();
        bindViewsRegister();
        showLogin();

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoRegister.setOnClickListener(v -> showRegister());
        tvGoLogin.setOnClickListener(v -> showLogin());
        tvMerchantLogin.setOnClickListener(v -> preFillMerchantDemo());
    }

    private void bindViewsLogin() {
        layoutLogin = findViewById(R.id.layoutLogin);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        tvMerchantLogin = findViewById(R.id.tvMerchantLogin);
    }

    private void bindViewsRegister() {
        layoutRegister = findViewById(R.id.layoutRegister);
        etRegName = findViewById(R.id.etRegName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);
    }

    private void showLogin() {
        layoutLogin.setVisibility(View.VISIBLE);
        layoutRegister.setVisibility(View.GONE);
    }

    private void showRegister() {
        layoutLogin.setVisibility(View.GONE);
        layoutRegister.setVisibility(View.VISIBLE);
    }

    private void preFillMerchantDemo() {
        etLoginEmail.setText("merchant@demo.com");
        etLoginPassword.setText("merchant123");
    }

    private void attemptLogin() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            AppUtils.showToast(this, "Please fill in all fields");
            return;
        }

        if (!AppUtils.isValidEmail(email)) {
            AppUtils.showToast(this, "Invalid email format");
            return;
        }

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        apiService.login(credentials).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (isFinishing() || isDestroyed())
                    return;
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> data = response.body();
                    long id = Long.parseLong(data.getOrDefault("id", "0"));
                    String role = data.getOrDefault("role", "customer");
                    session.createSession(id, data.get("name"), email, data.get("phone"), role, data.get("address"));
                    String token = data.getOrDefault("token", "");
                    if (token != null && !token.isEmpty())
                        session.setToken(token);
                    navigateAfterLogin(role);
                } else {
                    AppUtils.showToast(AuthActivity.this, "Invalid email or password");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (isFinishing() || isDestroyed())
                    return;
                AppUtils.showToast(AuthActivity.this, "Network Error: " + t.getMessage());
            }
        });
    }

    private void attemptRegister() {
        String name = etRegName.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String phone = etRegPhone.getText().toString().trim();
        String pass = etRegPassword.getText().toString().trim();
        String confirm = etRegConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(pass)) {
            AppUtils.showToast(this, "Please fill in all fields");
            return;
        }
        if (!AppUtils.isValidEmail(email)) {
            AppUtils.showToast(this, "Invalid email format");
            return;
        }
        if (!AppUtils.isValidPassword(pass)) {
            AppUtils.showToast(this, "Password must be at least 6 characters");
            return;
        }
        if (!pass.equals(confirm)) {
            AppUtils.showToast(this, "Passwords do not match");
            return;
        }

        int selectedId = rgRole.getCheckedRadioButtonId();
        android.widget.RadioButton rbSelected = findViewById(selectedId);
        String role = (rbSelected != null) ? rbSelected.getText().toString().toUpperCase() : "CUSTOMER";

        RegisterRequest request = new RegisterRequest(
                name,
                email,
                pass,
                phone,
                "", // address (optional in UI for now)
                role
        );
        apiService.register(request).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (isFinishing() || isDestroyed())
                    return;
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> data = response.body();
                    long id = Long.parseLong(data.getOrDefault("id", "0"));
                    String role = data.getOrDefault("role", "customer");
                    String token = data.getOrDefault("token", "");
                    session.createSession(id, data.get("name"), data.get("email"),
                            data.get("phone"), role, data.getOrDefault("address", ""));
                    if (token != null && !token.isEmpty()) {
                        session.setToken(token);
                    }
                    navigateAfterLogin(role);
                } else {
                    String errMsg = "Registration failed (code: " + response.code() + ")";
                    if (response.code() == 400)
                        errMsg = "Email already registered!";
                    AppUtils.showToast(AuthActivity.this, errMsg);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (isFinishing() || isDestroyed())
                    return;
                AppUtils.showToast(AuthActivity.this, "Network Error: " + t.getMessage());
            }
        });
    }

    private void navigateAfterLogin(String role) {
        Intent intent;
        if ("merchant".equalsIgnoreCase(role)) {
            intent = new Intent(this, MerchantMainActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}