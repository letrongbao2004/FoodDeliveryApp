package com.fooddeliveryapp.remote;

import android.content.Context;
import com.fooddeliveryapp.managers.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static Retrofit retrofit = null;

    /** Call this if you need to reset the singleton (e.g., after logout) */
    public static void reset() { retrofit = null; }

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

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
