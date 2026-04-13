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
import android.content.Intent;
import com.fooddeliveryapp.activities.OrderDetailActivity;

import java.util.List;

public class MerchantOrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private OrderManager orderManager;

    @Nullable
    @Override
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
        // Navigate directly to the STOMP managed Order Detail Activity
        Intent intent = new Intent(requireContext(), OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    @Override
    public void onTrackOrder(Order order) {
        onOrderClick(order);
    }
}