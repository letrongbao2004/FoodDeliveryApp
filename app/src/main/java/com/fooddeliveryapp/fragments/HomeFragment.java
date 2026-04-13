package com.fooddeliveryapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.activities.RestaurantDetailActivity;
import com.fooddeliveryapp.adapters.RestaurantAdapter;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements RestaurantAdapter.OnRestaurantClickListener {

    private ApiService apiService;
    private SessionManager session;
    private RecyclerView rvRestaurants;
    private RestaurantAdapter adapter;
    private EditText etSearch;
    private TextView tvGreeting;
    
    // In-memory cache for fast searching & filtering
    private List<Restaurant> allRestaurants = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getClient(requireContext()).create(ApiService.class);
        session = SessionManager.getInstance(requireContext());

        tvGreeting   = view.findViewById(R.id.tvHomeGreeting);
        etSearch     = view.findViewById(R.id.etHomeSearch);
        rvRestaurants= view.findViewById(R.id.rvHomeRestaurants);

        // Assuming session manager works
        String name = session.getName() == null ? "User" : session.getName();
        tvGreeting.setText("Hello, " + name + " 👋");

        // Prepare empty adapter so RecyclerView doesn't crash before async finishes
        adapter = new RestaurantAdapter(requireContext(), new ArrayList<>(), this);
        rvRestaurants.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRestaurants.setAdapter(adapter);

        loadRestaurants();
        setupSearch();
        setupCategoryFilters(view);
    }

    private void loadRestaurants() {
        apiService.getRestaurants().enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    allRestaurants = response.body();
                    adapter.updateData(allRestaurants);
                } else {
                    Toast.makeText(requireContext(), "Failed to load restaurants", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String query = s.toString().trim().toLowerCase();
                if (query.isEmpty()) {
                    adapter.updateData(allRestaurants);
                } else {
                    List<Restaurant> filtered = new ArrayList<>();
                    for (Restaurant r : allRestaurants) {
                        if (r.getName().toLowerCase().contains(query) || 
                           (r.getCategory() != null && r.getCategory().toLowerCase().contains(query))) {
                            filtered.add(r);
                        }
                    }
                    adapter.updateData(filtered);
                }
            }
        });
    }

    private void setupCategoryFilters(View view) {
        int[] chipIds = {R.id.chipAll, R.id.chipBurgers, R.id.chipPizza,
                         R.id.chipSushi, R.id.chipChicken, R.id.chipFreeDelivery};
        String[] categories = {null, "Burgers", "Pizza", "Japanese", "Chicken", null};
        boolean[] freeDelivery = {false, false, false, false, false, true};

        for (int i = 0; i < chipIds.length; i++) {
            final String cat = categories[i];
            final boolean fd = freeDelivery[i];
            View chip = view.findViewById(chipIds[i]);
            if (chip != null) {
                chip.setOnClickListener(v -> {
                    List<Restaurant> filtered = new ArrayList<>();
                    for (Restaurant r : allRestaurants) {
                        boolean matchCategory = (cat == null) || (r.getCategory() != null && r.getCategory().equalsIgnoreCase(cat));
                        boolean matchDelivery = (!fd) || (r.getDeliveryFee() == 0);
                        if (matchCategory && matchDelivery) {
                            filtered.add(r);
                        }
                    }
                    adapter.updateData(filtered);
                });
            }
        }
    }

    @Override
    public void onRestaurantClick(Restaurant restaurant) {
        Intent intent = new Intent(requireContext(), RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurant.getId());
        startActivity(intent);
    }
}
