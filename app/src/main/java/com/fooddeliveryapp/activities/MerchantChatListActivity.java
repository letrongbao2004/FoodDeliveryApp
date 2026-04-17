package com.fooddeliveryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.ChatThreadAdapter;
import com.fooddeliveryapp.models.ChatThread;
import com.fooddeliveryapp.models.Restaurant;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MerchantChatListActivity extends AppCompatActivity implements ChatThreadAdapter.OnThreadClickListener {

    private RecyclerView rvThreads;
    private ChatThreadAdapter adapter;
    private List<ChatThread> threadList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_chat_list);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Customer Messages");
        }

        rvThreads = findViewById(R.id.rvMerchantChatThreads);
        rvThreads.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatThreadAdapter(this, threadList, this);
        rvThreads.setAdapter(adapter);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        fetchMyRestaurantAndThreads();
    }

    private void fetchMyRestaurantAndThreads() {
        long ownerId = SessionManager.getInstance(this).getUserId();
        apiService.getRestaurantByOwner(ownerId).enqueue(new Callback<Restaurant>() {
            @Override
            public void onResponse(Call<Restaurant> call, Response<Restaurant> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    loadRestaurantThreads(response.body().getId());
                } else {
                    Toast.makeText(MerchantChatListActivity.this, "Restaurant not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Restaurant> call, Throwable t) {
                Toast.makeText(MerchantChatListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRestaurantThreads(long restaurantId) {
        apiService.getRestaurantChatThreads(restaurantId).enqueue(new Callback<List<ChatThread>>() {
            @Override
            public void onResponse(Call<List<ChatThread>> call, Response<List<ChatThread>> response) {
                if (isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    threadList.clear();
                    threadList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ChatThread>> call, Throwable t) {
                Toast.makeText(MerchantChatListActivity.this, "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onThreadClick(ChatThread thread) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("user_id", thread.getUserId());
        intent.putExtra("restaurant_id", thread.getRestaurantId());
        intent.putExtra("titleName", thread.getParticipantName());
        startActivity(intent);
    }
}
