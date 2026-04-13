package com.fooddeliveryapp.api.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MoMoConfig {
    public static final String PARTNER_CODE = "MOMOBKUN20180529";
    public static final String ACCESS_KEY = "klm05TvNBzhg7h7j";
    public static final String SECRET_KEY = "at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa";
    public static final String CREATE_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    
    // Replace with your actual domain when deploying
    public static final String REDIRECT_URL = "http://localhost:8080/api/payment/return";
    public static final String NOTIFY_URL = "http://localhost:8080/api/payment/notify";

    // Merchant phone number linked to MoMo account
    public static final String PARTNER_PHONE = "0378676412";
}
