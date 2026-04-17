package com.fooddeliveryapp;

import android.app.Application;

import com.fooddeliveryapp.utils.CrashReporter;

public class FoodDeliveryApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReporter.install(this);
    }
}
