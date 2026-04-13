package com.fooddeliveryapp.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * General utility/helper methods.
 */
public class AppUtils {

    public static final double EXCHANGE_RATE_VND = 25000.0;

    private AppUtils() {
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackbar(View root, String message) {
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    public static boolean isValidEmail(String email) {
        return email != null &&
                Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                        .matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\+?[0-9]{9,13}");
    }

    public static String formatPrice(double price) {
        double priceInVnd = price * EXCHANGE_RATE_VND;
        return formatVnd(priceInVnd);
    }

    public static String formatVnd(double priceInVnd) {
        return String.format(java.util.Locale.US, "%,.0f đ", priceInVnd);
    }

    public static String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US);
        return sdf.format(new Date(millis));
    }

    public static String formatDateShort(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.US);
        return sdf.format(new Date(millis));
    }

    /** Returns a color resource int for each order status string. */
    public static int getStatusColor(String status) {
        switch (status) {
            case "Pending":
                return android.R.color.holo_orange_light;
            case "Confirmed":
                return android.R.color.holo_blue_light;
            case "Preparing":
                return android.R.color.holo_blue_dark;
            case "Delivering":
                return android.R.color.holo_purple;
            case "Delivered":
                return android.R.color.holo_green_dark;
            case "Cancelled":
                return android.R.color.holo_red_light;
            default:
                return android.R.color.darker_gray;
        }
    }

    /** Builds a placeholder image URL by color for use with color-coded cards. */
    public static String getPlaceholderImageUrl(String category) {
        switch (category) {
            case "Burgers":
                return "https://via.placeholder.com/300x200/FF6B35/FFFFFF?text=Burger";
            case "Pizza":
                return "https://via.placeholder.com/300x200/F7931E/FFFFFF?text=Pizza";
            case "Japanese":
                return "https://via.placeholder.com/300x200/C0392B/FFFFFF?text=Sushi";
            case "Mexican":
                return "https://via.placeholder.com/300x200/27AE60/FFFFFF?text=Tacos";
            case "Asian":
                return "https://via.placeholder.com/300x200/8E44AD/FFFFFF?text=Noodles";
            case "Chicken":
                return "https://via.placeholder.com/300x200/E67E22/FFFFFF?text=Chicken";
            default:
                return "https://via.placeholder.com/300x200/95A5A6/FFFFFF?text=Food";
        }
    }
}