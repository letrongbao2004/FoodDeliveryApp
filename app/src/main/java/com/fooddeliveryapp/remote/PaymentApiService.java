package com.fooddeliveryapp.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentApiService {

    /** Returns payUrl as plain String */
    @GET("payment/create")
    Call<String> createPayment(@Query("amount") int amount);

    /** Check order status: PENDING | SUCCESS | FAILED */
    @GET("order/{id}")
    Call<String> getOrderStatus(@Path("id") String orderId);
}