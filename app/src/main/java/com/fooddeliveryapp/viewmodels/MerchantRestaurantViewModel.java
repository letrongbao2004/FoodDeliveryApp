package com.fooddeliveryapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fooddeliveryapp.models.Advertisement;
import com.fooddeliveryapp.models.Food;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.remote.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantRestaurantViewModel extends ViewModel {

    private final MutableLiveData<Restaurant> restaurant = new MutableLiveData<>();
    private final MutableLiveData<List<Food>> foods = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Advertisement>> ads = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<Restaurant> getRestaurant() { return restaurant; }
    public LiveData<List<Food>> getFoods() { return foods; }
    public LiveData<List<Advertisement>> getAds() { return ads; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void fetchRestaurantDetails(ApiService apiService, long restaurantId) {
        isLoading.setValue(true);
        apiService.getRestaurants().enqueue(new Callback<List<Restaurant>>() {
            @Override
            public void onResponse(Call<List<Restaurant>> call, Response<List<Restaurant>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    for (Restaurant r : response.body()) {
                        if (r.getId() == restaurantId) {
                            restaurant.setValue(r);
                            fetchFoods(apiService, restaurantId);
                            fetchAds(apiService, restaurantId);
                            return;
                        }
                    }
                    errorMessage.setValue("Restaurant not found");
                } else {
                    errorMessage.setValue("Failed to load restaurant details");
                }
            }

            @Override
            public void onFailure(Call<List<Restaurant>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
            }
        });
    }

    private void fetchFoods(ApiService apiService, long restaurantId) {
        apiService.getFoods((int) restaurantId).enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(Call<List<Food>> call, Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    foods.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Food>> call, Throwable t) {
                // Silently fail or log
            }
        });
    }

    private void fetchAds(ApiService apiService, long restaurantId) {
        apiService.getAds(10).enqueue(new Callback<List<Advertisement>>() {
            @Override
            public void onResponse(Call<List<Advertisement>> call, Response<List<Advertisement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Advertisement> filtered = new ArrayList<>();
                    for (Advertisement ad : response.body()) {
                        if (ad.getMenuItem() != null && ad.getMenuItem().getRestaurantId() == restaurantId) {
                            filtered.add(ad);
                        }
                    }
                    ads.setValue(filtered);
                }
            }

            @Override
            public void onFailure(Call<List<Advertisement>> call, Throwable t) {
            }
        });
    }

    public void refresh(ApiService apiService, long restaurantId) {
        fetchRestaurantDetails(apiService, restaurantId);
    }
}
