package com.fooddeliveryapp.fragments.merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.AppUtils;
import com.fooddeliveryapp.utils.SessionManager;

public class MerchantDashboardFragment extends Fragment {

    private ApiService apiService;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        SessionManager session = SessionManager.getInstance(requireContext());

        // Hard-coded restaurant ID 1 for demo merchant
        int restaurantId = 1;

        TextView tvRevenue    = view.findViewById(R.id.tvDashRevenue);
        TextView tvTotalOrders= view.findViewById(R.id.tvDashTotalOrders);
        TextView tvNewOrders  = view.findViewById(R.id.tvDashNewOrders);

        // Note: Waiting for Backend endpoint `/api/restaurants/{id}/stats`
        // Providing mocked zero states temporarily to fix removed SQLite compilation errors
        double revenue = 0.0;
        int totalOrders = 0;

        tvRevenue.setText(AppUtils.formatPrice(revenue));
        tvTotalOrders.setText(String.valueOf(totalOrders));
        tvNewOrders.setText("0"); // Demo value
    }
}
