package com.fooddeliveryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.utils.AppUtils;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);

        void onTrackOrder(Order order);
    }

    private final Context context;
    private final List<Order> orders;
    private final OnOrderClickListener listener;

    public OrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvOrderCode.setText(order.getOrderCode());
        holder.tvRestaurantName.setText(order.getRestaurantName());
        holder.tvTotal.setText(order.getTotalText());
        holder.tvStatus.setText(order.getStatus());
        holder.tvDate.setText(AppUtils.formatDate(order.getCreatedAt()));
        holder.tvStatusBadge.setText(order.getStatus());
        holder.tvStatusBadge.setTextColor(
                context.getColor(AppUtils.getStatusColor(order.getStatus())));

        boolean isActive = !order.getStatus().equals(Order.STATUS_DELIVERED)
                && !order.getStatus().equals(Order.STATUS_CANCELLED);
        holder.btnTrack.setVisibility(isActive ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
        holder.btnTrack.setOnClickListener(v -> listener.onTrackOrder(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvRestaurantName, tvTotal, tvStatus,
                tvDate, tvStatusBadge, btnTrack;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvRestaurantName = itemView.findViewById(R.id.tvOrderRestaurantName);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvStatusBadge = itemView.findViewById(R.id.tvOrderStatusBadge);
            btnTrack = itemView.findViewById(R.id.btnTrackOrder);
        }
    }
}