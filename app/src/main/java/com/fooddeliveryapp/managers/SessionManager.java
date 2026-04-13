package com.fooddeliveryapp.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FoodDeliveryAuthPref";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private static SessionManager instance;

    private SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    // Save JWT Token
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Get JWT Token
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    // Save current User ID
    public void saveUserId(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    // Get current User ID
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    // Check if logged in
    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // Clear session (Logout)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}