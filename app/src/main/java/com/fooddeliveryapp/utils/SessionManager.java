package com.fooddeliveryapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager – persists logged-in user info using SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "FoodDeliverySession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LOGGED = "is_logged_in";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private static SessionManager instance;

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null)
            instance = new SessionManager(context.getApplicationContext());
        return instance;
    }

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createSession(long userId, String name, String email,
            String phone, String role, String address) {
        editor.putBoolean(KEY_LOGGED, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ROLE, role == null ? "customer" : role.trim());
        editor.putString(KEY_ADDRESS, address);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getRole() {
        String role = prefs.getString(KEY_ROLE, "customer");
        return role == null ? "customer" : role.trim();
    }

    public String getAddress() {
        return prefs.getString(KEY_ADDRESS, "");
    }

    public boolean isMerchant() {
        return "merchant".equalsIgnoreCase(getRole());
    }

    public void updateName(String name) {
        editor.putString(KEY_NAME, name).apply();
    }

    public void updatePhone(String phone) {
        editor.putString(KEY_PHONE, phone).apply();
    }

    public void updateAddress(String addr) {
        editor.putString(KEY_ADDRESS, addr).apply();
    }

    public void setToken(String token) {
        editor.putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void setAvatarUrl(String url) {
        editor.putString(KEY_AVATAR_URL, url).apply();
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, null);
    }

    public void logout() {
        editor.clear().apply();
    }
}