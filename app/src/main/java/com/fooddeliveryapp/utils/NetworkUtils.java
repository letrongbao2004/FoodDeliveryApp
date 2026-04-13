package com.fooddeliveryapp.utils;

import retrofit2.Response;

public class NetworkUtils {

    public static String readError(Response<?> response) {
        if (response == null) return null;
        try {
            if (response.errorBody() == null) return null;
            String s = response.errorBody().string();
            if (s == null) return null;
            s = s.trim();
            return s.isEmpty() ? null : s;
        } catch (Exception ignored) {
            return null;
        }
    }
}

