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
import com.fooddeliveryapp.models.OrderDetail;
import com.fooddeliveryapp.utils.AppUtils;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderDetail.OrderDetailItem> items;

    public OrderDetailAdapter(Context context, List<OrderDetail.OrderDetailItem> items) {
        this.context = context;
        this.items   = items;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        OrderDetail.OrderDetailItem item = items.get(position);
        h.tvName.setText(item.getFoodName());
        h.tvQty.setText("x" + item.getQuantity());
        h.tvPrice.setText(AppUtils.formatPrice(item.getPrice()));
        h.tvLineTotal.setText(AppUtils.formatPrice(item.getLineTotal()));

        if (item.getFoodImageUrl() != null && !item.getFoodImageUrl().isEmpty()) {
            Glide.with(context).load(item.getFoodImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(h.ivFood);
        }
    }

    @Override public int getItemCount() { return items != null ? items.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvName, tvQty, tvPrice, tvLineTotal;
        ViewHolder(View v) {
            super(v);
            ivFood      = v.findViewById(R.id.ivOrderDetailFood);
            tvName      = v.findViewById(R.id.tvOrderDetailFoodName);
            tvQty       = v.findViewById(R.id.tvOrderDetailQty);
            tvPrice     = v.findViewById(R.id.tvOrderDetailPrice);
            tvLineTotal = v.findViewById(R.id.tvOrderDetailLineTotal);
        }
    }
}
