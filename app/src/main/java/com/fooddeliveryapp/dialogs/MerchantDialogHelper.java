package com.fooddeliveryapp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class MerchantDialogHelper {

    public interface OnDialogSubmitListener<T> {
        void onSubmit(T request);
    }

    public static void showEditRestaurantDialog(Context context, Restaurant restaurant, OnDialogSubmitListener<Restaurant> listener) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_edit_restaurant, null);
        EditText etName = v.findViewById(R.id.etEditRestaurantName);
        EditText etCategory = v.findViewById(R.id.etEditRestaurantCategory);
        EditText etDesc = v.findViewById(R.id.etEditRestaurantDesc);
        EditText etAddress = v.findViewById(R.id.etEditRestaurantAddress);
        EditText etPhone = v.findViewById(R.id.etEditRestaurantPhone);

        etName.setText(restaurant.getName());
        etCategory.setText(restaurant.getCategory());
        etDesc.setText(restaurant.getDescription());
        etAddress.setText(restaurant.getAddress());
        etPhone.setText(restaurant.getPhone());

        new AlertDialog.Builder(context)
                .setTitle("Edit Restaurant")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {
                    restaurant.setName(etName.getText().toString().trim());
                    restaurant.setCategory(etCategory.getText().toString().trim());
                    restaurant.setDescription(etDesc.getText().toString().trim());
                    restaurant.setAddress(etAddress.getText().toString().trim());
                    restaurant.setPhone(etPhone.getText().toString().trim());
                    listener.onSubmit(restaurant);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void showAddFoodDialog(Context context, View.OnClickListener onImageClick, OnDialogSubmitListener<FoodData> listener) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_add_food, null);
        EditText etName = v.findViewById(R.id.etAddFoodName);
        EditText etDesc = v.findViewById(R.id.etAddFoodDesc);
        EditText etPrice = v.findViewById(R.id.etAddFoodPrice);
        EditText etCat = v.findViewById(R.id.etAddFoodCategory);
        ImageView ivPreview = v.findViewById(R.id.ivAddFoodImage);
        TextView btnPick = v.findViewById(R.id.btnPickFoodImage);

        btnPick.setOnClickListener(v3 -> {
            ivPreview.setTag(ivPreview);
            onImageClick.onClick(ivPreview);
        });

        new AlertDialog.Builder(context)
                .setTitle("Add New Food")
                .setView(v)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String price = etPrice.getText().toString().trim();
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(price)) {
                        listener.onSubmit(new FoodData(name, etDesc.getText().toString(), Double.parseDouble(price), etCat.getText().toString(), ivPreview));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    public static void showAddAdDialog(Context context, List<Food> foods, View.OnClickListener onImageClick, OnDialogSubmitListener<AdData> listener) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_add_ad, null);
        EditText etTitle = v.findViewById(R.id.etAdTitle);
        EditText etDesc = v.findViewById(R.id.etAdDescription);
        Spinner spFood = v.findViewById(R.id.spAdFood);
        ImageView ivPreview = v.findViewById(R.id.ivAdImage);
        TextView btnPick = v.findViewById(R.id.btnPickAdImage);

        btnPick.setOnClickListener(v3 -> {
            ivPreview.setTag(ivPreview);
            onImageClick.onClick(ivPreview);
        });

        List<String> names = new ArrayList<>();
        for (Food f : foods) names.add(f.getName());
        spFood.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, names));

        new AlertDialog.Builder(context)
                .setTitle("Create Advertisement")
                .setView(v)
                .setPositiveButton("Create", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    if (!TextUtils.isEmpty(title)) {
                        listener.onSubmit(new AdData(title, etDesc.getText().toString(), foods.get(spFood.getSelectedItemPosition()), ivPreview));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static class FoodData {
        public String name, desc, cat;
        public double price;
        public ImageView preview;
        public FoodData(String n, String d, double p, String c, ImageView v) { name=n; desc=d; price=p; cat=c; preview=v; }
    }

    public static class AdData {
        public String title, desc;
        public Food food;
        public ImageView preview;
        public AdData(String t, String d, Food f, ImageView v) { title=t; desc=d; food=f; preview=v; }
    }
}
