package com.fooddeliveryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.utils.AppUtils;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    private final Context context;
    private List<Restaurant> restaurants;
    private final OnRestaurantClickListener listener;

    public RestaurantAdapter(Context context, List<Restaurant> restaurants,
            OnRestaurantClickListener listener) {
        this.context = context;
        this.restaurants = restaurants;
        this.listener = listener;
    }

    public void updateData(List<Restaurant> newList) {
        this.restaurants = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant r = restaurants.get(position);
        holder.tvName.setText(r.getName());
        holder.tvCategory.setText(r.getCategory());
        holder.tvRating.setText(r.getRatingText());
        holder.tvDistance.setText(r.getDistanceText());
        holder.tvDeliveryTime.setText(r.getDeliveryTimeText());
        holder.tvDeliveryFee.setText(
                r.getDeliveryFee() == 0 ? "Free Delivery" : AppUtils.formatPrice(r.getDeliveryFee()) + " delivery");
        holder.tvStatus.setText(r.isOpen() ? "Open" : "Closed");
        holder.tvStatus.setTextColor(context.getColor(
                r.isOpen() ? R.color.success : R.color.danger));

        if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
            Glide.with(context).load(r.getImageUrl()).centerCrop().into(holder.ivRestaurant);
        } else {
            holder.ivRestaurant.setImageResource(R.mipmap.ic_launcher);
        }

        // Badge visibility
        holder.tvBadgeFree.setVisibility(r.isHasFreeDelivery() ? View.VISIBLE : View.GONE);
        holder.tvBadgePromo.setVisibility(r.isHasPromo() ? View.VISIBLE : View.GONE);
        holder.tvBadgeFeatured.setVisibility(r.isFeatured() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onRestaurantClick(r));
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRestaurant;
        TextView tvName, tvCategory, tvRating, tvDistance,
                tvDeliveryTime, tvDeliveryFee, tvStatus,
                tvBadgeFree, tvBadgePromo, tvBadgeFeatured;

        ViewHolder(View itemView) {
            super(itemView);
            ivRestaurant = itemView.findViewById(R.id.ivRestaurant);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDeliveryTime = itemView.findViewById(R.id.tvDeliveryTime);
            tvDeliveryFee = itemView.findViewById(R.id.tvDeliveryFee);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBadgeFree = itemView.findViewById(R.id.tvBadgeFree);
            tvBadgePromo = itemView.findViewById(R.id.tvBadgePromo);
            tvBadgeFeatured = itemView.findViewById(R.id.tvBadgeFeatured);
        }
    }
}