package com.fooddeliveryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartActionListener {
        void onIncrease(CartItem item, int position);
        void onDecrease(CartItem item, int position);
        void onRemove(CartItem item, int position);
    }

    private final Context context;
    private final List<CartItem> items;
    private final OnCartActionListener listener;

    public CartAdapter(Context context, List<CartItem> items, OnCartActionListener listener) {
        this.context  = context;
        this.items    = items;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvName.setText(item.getFood().getName());
        holder.tvPrice.setText(item.getTotalPriceText());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        if (item.getSelectedSize() != null && !item.getSelectedSize().isEmpty()) {
            holder.tvCustomization.setVisibility(View.VISIBLE);
            holder.tvCustomization.setText("Size: " + item.getSelectedSize() +
                    (item.getSelectedSpice() != null ? " | Spice: " + item.getSelectedSpice() : ""));
        } else {
            holder.tvCustomization.setVisibility(View.GONE);
        }

        holder.btnIncrease.setOnClickListener(v -> listener.onIncrease(item, position));
        holder.btnDecrease.setOnClickListener(v -> listener.onDecrease(item, position));
        holder.btnRemove.setOnClickListener(v -> listener.onRemove(item, position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    public void updateItem(int position) {
        notifyItemChanged(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvName, tvPrice, tvQuantity, tvCustomization;
        TextView btnIncrease, btnDecrease, btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            ivFood          = itemView.findViewById(R.id.ivCartFood);
            tvName          = itemView.findViewById(R.id.tvCartFoodName);
            tvPrice         = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity      = itemView.findViewById(R.id.tvCartQuantity);
            tvCustomization = itemView.findViewById(R.id.tvCartCustomization);
            btnIncrease     = itemView.findViewById(R.id.btnCartIncrease);
            btnDecrease     = itemView.findViewById(R.id.btnCartDecrease);
            btnRemove       = itemView.findViewById(R.id.btnCartRemove);
        }
    }
}
