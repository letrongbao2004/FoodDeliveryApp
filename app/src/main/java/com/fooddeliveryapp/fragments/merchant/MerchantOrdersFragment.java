package com.fooddeliveryapp.fragments.merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.OrderAdapter;
import com.fooddeliveryapp.managers.OrderManager;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.utils.AppUtils;

import java.util.List;

public class MerchantOrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private OrderManager orderManager;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderManager = OrderManager.getInstance(requireContext());
        RecyclerView rvOrders = view.findViewById(R.id.rvMerchantOrders);

        orderManager.getRestaurantOrders(1, new OrderManager.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                OrderAdapter adapter = new OrderAdapter(requireContext(), orders, MerchantOrdersFragment.this);
                rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
                rvOrders.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {
                AppUtils.showToast(requireContext(), "Failed to load orders");
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        // Show status update dialog
        String[] statuses = {Order.STATUS_CONFIRMED, Order.STATUS_PREPARING,
                             Order.STATUS_DELIVERING, Order.STATUS_DELIVERED,
                             Order.STATUS_CANCELLED};
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Update Order " + order.getOrderCode())
            .setItems(statuses, (dialog, which) -> {
                orderManager.updateOrderStatus(order.getId(), statuses[which], new OrderManager.OrderCallback() {
                    @Override
                    public void onSuccess(Order updatedOrder) {
                        AppUtils.showToast(requireContext(), "Status updated to: " + statuses[which]);
                        // Refresh to fetch new statuses natively
                        onResume();
                    }

                    @Override
                    public void onError(String message) {
                        AppUtils.showToast(requireContext(), "Fail: " + message);
                    }
                });
            })
            .show();
    }

    @Override
    public void onTrackOrder(Order order) { onOrderClick(order); }
}
