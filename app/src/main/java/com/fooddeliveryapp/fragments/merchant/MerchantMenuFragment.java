package com.fooddeliveryapp.fragments.merchant;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.FoodAdapter;
import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class MerchantMenuFragment extends Fragment implements FoodAdapter.OnFoodClickListener {

    private ApiService apiService;
    private FoodAdapter adapter;
    private List<Food> foods = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        RecyclerView rvMenu = view.findViewById(R.id.rvMerchantMenu);
        View btnAddFood = view.findViewById(R.id.btnAddFood);

        // Merchant always has full access to manage menu (true = no closed-state
        // disabling)
        adapter = new FoodAdapter(requireContext(), foods, this, true);
        rvMenu.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMenu.setAdapter(adapter);

        loadFoods();

        btnAddFood.setOnClickListener(v -> showAddFoodDialog());
    }

    private void loadFoods() {
        apiService.getFoods(1).enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (!isAdded() || getContext() == null)
                    return;
                if (response.isSuccessful() && response.body() != null) {
                    foods.clear();
                    foods.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                if (!isAdded() || getContext() == null)
                    return;
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddFoodDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_food, null);
        EditText etName = dialogView.findViewById(R.id.etAddFoodName);
        EditText etDesc = dialogView.findViewById(R.id.etAddFoodDesc);
        EditText etPrice = dialogView.findViewById(R.id.etAddFoodPrice);
        EditText etCat = dialogView.findViewById(R.id.etAddFoodCategory);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Food")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String cat = etCat.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                        AppUtils.showToast(requireContext(), "Name and price are required");
                        return;
                    }

                    // Note: Need Backend @POST endpoint to truly save this.
                    AppUtils.showToast(requireContext(), "[Demo] Network Save Pending API Endpoint");

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onFoodClick(Food food) {
        new AlertDialog.Builder(requireContext())
                .setTitle(food.getName())
                .setMessage("What would you like to do?")
                .setPositiveButton("Delete", (d, w) -> {
                    // Note: Need Backend @DELETE endpoint to truly delete this.
                    AppUtils.showToast(requireContext(), "[Demo] Delete Pending API Endpoint");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAddToCartClick(Food food) {
        /* Not applicable for merchant */ }
}