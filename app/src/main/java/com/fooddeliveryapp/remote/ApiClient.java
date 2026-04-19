package com.fooddeliveryapp.remote;

import android.content.Context;
import android.os.Build;
import com.fooddeliveryapp.utils.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // Emulator uses 10.0.2.2 to reach host machine; real devices must use host's LAN IP.
    private static final String BASE_URL_EMULATOR = "http://10.0.2.2:8080/api/";
    private static final String BASE_URL_DEVICE = "http://192.168.1.7:8080/api/";
    private static Retrofit retrofit = null;

    /** Call this if you need to reset the singleton (e.g., after logout) */
    public static void reset() {
        retrofit = null;
    }

    private static boolean isEmulator() {
        String fingerprint = Build.FINGERPRINT;
        String model = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        String brand = Build.BRAND;
        String device = Build.DEVICE;
        String product = Build.PRODUCT;

        return (fingerprint != null && (fingerprint.startsWith("generic") || fingerprint.startsWith("unknown")))
                || (model != null && (model.contains("google_sdk") || model.contains("Emulator") || model.contains("Android SDK built for x86")))
                || (manufacturer != null && manufacturer.contains("Genymotion"))
                || (brand != null && brand.startsWith("generic") && device != null && device.startsWith("generic"))
                || ("google_sdk".equals(product));
    }

    private static String getBaseUrl() {
        return isEmulator() ? BASE_URL_EMULATOR : BASE_URL_DEVICE;
    }

    public static String getWebSocketUrl() {
        // Strip out "api/" and replace "http" with "ws"
        String rawBase = isEmulator() ? BASE_URL_EMULATOR : BASE_URL_DEVICE;
        return rawBase.replace("http://", "ws://").replace("/api/", "/ws/websocket");
    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request.Builder requestBuilder = chain.request().newBuilder();

                        // Attach JWT Token if it exists
                        SessionManager sessionManager = SessionManager.getInstance(context);
                        String token = sessionManager.getToken();
                        if (token != null && !token.isEmpty()) {
                            requestBuilder.addHeader("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}