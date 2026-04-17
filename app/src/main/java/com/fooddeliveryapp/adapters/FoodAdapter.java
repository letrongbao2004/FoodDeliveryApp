package com.fooddeliveryapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.Food;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    public interface OnFoodClickListener {
        void onFoodClick(Food food);

        void onAddToCartClick(Food food);
    }

    private final Context context;
    private List<Food> foods;
    private final OnFoodClickListener listener;

    /** true = nhà hàng đang mở cửa, false = đóng cửa */
    private boolean isRestaurantOpen;
    /** true = merchant mode (manage menu), false = customer mode */
    private final boolean isMerchantMode;

    public FoodAdapter(Context context, List<Food> foods,
            OnFoodClickListener listener, boolean isRestaurantOpen) {
        this(context, foods, listener, isRestaurantOpen, false);
    }

    public FoodAdapter(Context context, List<Food> foods,
            OnFoodClickListener listener, boolean isRestaurantOpen, boolean isMerchantMode) {
        this.context = context;
        this.foods = foods;
        this.listener = listener;
        this.isRestaurantOpen = isRestaurantOpen;
        this.isMerchantMode = isMerchantMode;
    }

    public void updateData(List<Food> newList) {
        this.foods = newList;
        notifyDataSetChanged();
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
        notifyDataSetChanged();
    }

    public void setRestaurantOpen(boolean open) {
        this.isRestaurantOpen = open;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foods.get(position);

        holder.tvName.setText(food.getName());
        holder.tvDescription.setText(food.getDescription());
        holder.tvPrice.setText(food.getPriceText());
        holder.tvCategory.setText(food.getCategory());

        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Glide.with(context).load(food.getImageUrl()).centerCrop().into(holder.ivFood);
        } else {
            holder.ivFood.setImageResource(R.mipmap.ic_launcher);
        }

        holder.tvBadgeBestSeller.setVisibility(food.isBestSeller() ? View.VISIBLE : View.GONE);
        holder.tvBadgeNew.setVisibility(food.isNew() ? View.VISIBLE : View.GONE);

        if (isMerchantMode) {
            // Merchant: allow managing regardless of availability/open state; hide add-to-cart button.
            holder.itemView.setAlpha(1f);
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(v -> listener.onFoodClick(food));

            holder.btnAddToCart.setVisibility(View.GONE);
            holder.btnAddToCart.setOnClickListener(null);
            holder.btnAddToCart.setEnabled(false);
            return;
        }

        if (!isRestaurantOpen) {
            // Nhà hàng đóng cửa: mờ toàn bộ item, disable nút
            holder.itemView.setAlpha(0.5f);
            holder.btnAddToCart.setEnabled(false);
            holder.btnAddToCart.setText("Closed");
            // Set background tint/resource instead of flat color to preserve corners
            holder.btnAddToCart.setBackgroundResource(R.drawable.bg_rounded_primary);
            holder.btnAddToCart.getBackground().mutate().setTint(Color.parseColor("#AAAAAA"));

            // Không cho click vào item
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
            holder.btnAddToCart.setOnClickListener(null); // Clear click

        } else if (!food.isAvailable()) {
            // Nhà hàng mở nhưng món tạm hết / không có sẵn
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setClickable(false);
            holder.itemView.setOnClickListener(null);

            holder.btnAddToCart.setEnabled(false);
            holder.btnAddToCart.setText("Unavailable");
            holder.btnAddToCart.setBackgroundResource(R.drawable.bg_rounded_primary);
            holder.btnAddToCart.getBackground().mutate().setTint(Color.parseColor("#AAAAAA"));
            holder.btnAddToCart.setOnClickListener(null); // Clear click

        } else {
            // Bình thường: nhà hàng mở, món có sẵn
            holder.itemView.setAlpha(1f);
            holder.itemView.setClickable(true);

            holder.btnAddToCart.setEnabled(true);
            holder.btnAddToCart.setText("+");
            holder.btnAddToCart.setBackgroundResource(R.drawable.bg_rounded_primary);
            holder.btnAddToCart.getBackground().mutate().setTintList(null); // Reset tint to default primary

            holder.itemView.setOnClickListener(v -> listener.onFoodClick(food));
            holder.btnAddToCart.setOnClickListener(v -> listener.onAddToCartClick(food));
        }
    }

    @Override
    public int getItemCount() {
        return foods == null ? 0 : foods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvName, tvDescription, tvPrice, tvCategory,
                tvBadgeBestSeller, tvBadgeNew, btnAddToCart;

        ViewHolder(View itemView) {
            super(itemView);
            ivFood = itemView.findViewById(R.id.ivFood);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDescription = itemView.findViewById(R.id.tvFoodDescription);
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvCategory = itemView.findViewById(R.id.tvFoodCategory);
            tvBadgeBestSeller = itemView.findViewById(R.id.tvBadgeBestSeller);
            tvBadgeNew = itemView.findViewById(R.id.tvBadgeNew);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}