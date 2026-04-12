package com.fooddeliveryapp.fragments;

import android.content.Intent;
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
import com.fooddeliveryapp.activities.OrderTrackingActivity;
import com.fooddeliveryapp.adapters.OrderAdapter;
import com.fooddeliveryapp.managers.OrderManager;
import com.fooddeliveryapp.models.Order;
import com.fooddeliveryapp.utils.SessionManager;

import java.util.List;

public class OrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private OrderManager orderManager;
    private SessionManager session;
    private RecyclerView rvOrders;
    private View layoutEmpty;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderManager = OrderManager.getInstance(requireContext());
        session      = SessionManager.getInstance(requireContext());

        rvOrders    = view.findViewById(R.id.rvOrderHistory);
        layoutEmpty = view.findViewById(R.id.layoutEmptyOrders);

        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        orderManager.getOrderHistory(session.getUserId(), new OrderManager.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (orders.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvOrders.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvOrders.setVisibility(View.VISIBLE);
                    OrderAdapter adapter = new OrderAdapter(requireContext(), orders, OrdersFragment.this);
                    rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
                    rvOrders.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvOrders.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(requireContext(), OrderTrackingActivity.class);
        intent.putExtra(OrderTrackingActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    @Override
    public void onTrackOrder(Order order) {
        onOrderClick(order);
    }
}
