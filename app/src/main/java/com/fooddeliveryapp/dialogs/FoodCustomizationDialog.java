package com.fooddeliveryapp.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.utils.AppUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FoodCustomizationDialog extends BottomSheetDialogFragment {

    public interface OnAddToCartListener {
        void onAddToCart(Food food, int qty, String size, String spice, String addOns, String notes);
    }

    private static final String ARG_FOOD_ID = "food_id";
    private static final String ARG_FOOD_NAME = "food_name";
    private static final String ARG_FOOD_PRICE = "food_price";
    private static final String ARG_FOOD_DESC = "food_desc";
    private static final String ARG_RESTAURANT_ID = "restaurant_id";
    private static final String ARG_IS_AVAILABLE = "is_available";

    private Food food;
    private OnAddToCartListener listener;
    private int quantity = 1;
    private double basePrice;

    public static FoodCustomizationDialog newInstance(Food food) {
        FoodCustomizationDialog dialog = new FoodCustomizationDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_FOOD_ID, food.getId());
        args.putString(ARG_FOOD_NAME, food.getName());
        args.putDouble(ARG_FOOD_PRICE, food.getPrice());
        args.putString(ARG_FOOD_DESC, food.getDescription());
        args.putInt(ARG_RESTAURANT_ID, food.getRestaurantId());
        args.putBoolean(ARG_IS_AVAILABLE, food.isAvailable());
        dialog.setArguments(args);
        dialog.food = food; // Keep reference if fragment not yet created
        return dialog;
    }

    public void setOnAddToCartListener(OnAddToCartListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_food_customization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (food == null && getArguments() != null) {
            food = new Food();
            food.setId(getArguments().getInt(ARG_FOOD_ID));
            food.setName(getArguments().getString(ARG_FOOD_NAME));
            food.setPrice(getArguments().getDouble(ARG_FOOD_PRICE));
            food.setDescription(getArguments().getString(ARG_FOOD_DESC));
            food.setRestaurantId(getArguments().getInt(ARG_RESTAURANT_ID));
            food.setAvailable(getArguments().getBoolean(ARG_IS_AVAILABLE, true));
        }

        if (food == null) {
            dismiss();
            return;
        } // safety guard

        basePrice = food.getPrice();

        TextView tvFoodName = view.findViewById(R.id.tvDialogFoodName);
        TextView tvFoodDesc = view.findViewById(R.id.tvDialogFoodDesc);
        TextView tvPrice = view.findViewById(R.id.tvDialogPrice);
        TextView tvQty = view.findViewById(R.id.tvDialogQuantity);
        TextView btnIncrease = view.findViewById(R.id.btnDialogIncrease);
        TextView btnDecrease = view.findViewById(R.id.btnDialogDecrease);
        TextView btnAdd = view.findViewById(R.id.btnDialogAddToCart);
        RadioGroup rgSize = view.findViewById(R.id.rgDialogSize);
        RadioGroup rgSpice = view.findViewById(R.id.rgDialogSpice);
        ChipGroup cgAddOns = view.findViewById(R.id.cgDialogAddOns);
        android.widget.EditText etNotes = view.findViewById(R.id.etDialogNotes);

        tvFoodName.setText(food.getName());
        tvFoodDesc.setText(food.getDescription() != null ? food.getDescription() : "");
        tvPrice.setText(AppUtils.formatPrice(basePrice * quantity));
        tvQty.setText(String.valueOf(quantity));

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQty.setText(String.valueOf(quantity));
            tvPrice.setText(AppUtils.formatPrice(basePrice * quantity));
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQty.setText(String.valueOf(quantity));
                tvPrice.setText(AppUtils.formatPrice(basePrice * quantity));
            }
        });

        btnAdd.setOnClickListener(v -> {
            if (listener == null) {
                dismiss();
                return;
            }

            // Size
            int sizeId = rgSize.getCheckedRadioButtonId();
            String size = "";
            if (sizeId != -1) {
                RadioButton rb = view.findViewById(sizeId);
                size = rb != null ? rb.getText().toString() : "";
            }

            // Spice
            int spiceId = rgSpice.getCheckedRadioButtonId();
            String spice = "";
            if (spiceId != -1) {
                RadioButton rb = view.findViewById(spiceId);
                spice = rb != null ? rb.getText().toString() : "";
            }

            // Add ons
            List<String> addOnList = new ArrayList<>();
            for (int i = 0; i < cgAddOns.getChildCount(); i++) {
                Chip chip = (Chip) cgAddOns.getChildAt(i);
                if (chip.isChecked())
                    addOnList.add(chip.getText().toString());
            }
            String addOns = String.join(", ", addOnList);
            String notes = etNotes.getText().toString().trim();

            listener.onAddToCart(food, quantity, size, spice, addOns, notes);
            dismiss();
        });
    }
}