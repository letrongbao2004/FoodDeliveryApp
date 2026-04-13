package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.PaymentApiService;
import com.fooddeliveryapp.utils.AppUtils;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "MoMoPayment";

    private TextView tvStatus, tvAmount;
    private ImageView ivQr;
    private Button btnPay;
    private ProgressBar progressBar;

    private int amount = 10000;
    private String payUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvStatus    = findViewById(R.id.tvPaymentStatus);
        tvAmount    = findViewById(R.id.tvPaymentAmount);
        ivQr        = findViewById(R.id.ivPaymentQr);
        btnPay      = findViewById(R.id.btnPayWithMoMo);
        progressBar = findViewById(R.id.progressPayment);

        if (getIntent().hasExtra("amount")) {
            amount = getIntent().getIntExtra("amount", 10000);
        }

        tvAmount.setText(AppUtils.formatVnd(amount));
        btnPay.setEnabled(false);

        // Button: open payUrl via Intent (ACTION_VIEW)
        btnPay.setOnClickListener(v -> {
            if (payUrl != null && !payUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(payUrl));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Payment URL not ready", Toast.LENGTH_SHORT).show();
            }
        });

        createMoMoPayment();
    }

    private void createMoMoPayment() {
        tvStatus.setText("Connecting to MoMo Sandbox...");
        progressBar.setVisibility(View.VISIBLE);
        btnPay.setEnabled(false);

        PaymentApiService apiService = ApiClient.getClient(this).create(PaymentApiService.class);
        apiService.createPayment(amount).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    payUrl = response.body().trim();
                    Log.d(TAG, "payUrl received: " + payUrl);

                    if (payUrl.startsWith("Error")) {
                        tvStatus.setText(payUrl);
                        return;
                    }

                    // Show QR code from payUrl
                    String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data="
                            + Uri.encode(payUrl);
                    Glide.with(PaymentActivity.this).load(qrImageUrl).into(ivQr);

                    tvStatus.setText("QR ready! Scan with phone camera or tap Pay button");
                    btnPay.setEnabled(true);

                } else {
                    String errBody = "";
                    try {
                        if (response.errorBody() != null) errBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Log.e(TAG, "HTTP " + response.code() + ": " + errBody);
                    tvStatus.setText("Error " + response.code() + ": " + errBody);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network failure: " + t.getMessage());
                tvStatus.setText("Cannot connect to backend.\nMake sure Spring Boot is running on port 8080.");
                Toast.makeText(PaymentActivity.this,
                        "Backend unreachable", Toast.LENGTH_LONG).show();
            }
        });
    }
}
