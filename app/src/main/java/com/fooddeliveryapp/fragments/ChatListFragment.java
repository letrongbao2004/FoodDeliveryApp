package com.fooddeliveryapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.activities.ChatActivity;
import com.fooddeliveryapp.adapters.ChatThreadAdapter;
import com.fooddeliveryapp.models.ChatThread;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment implements ChatThreadAdapter.OnThreadClickListener {

    private RecyclerView rvThreads;
    private ChatThreadAdapter adapter;
    private List<ChatThread> threadList = new ArrayList<>();
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);
        rvThreads = v.findViewById(R.id.rvChatThreads);
        rvThreads.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatThreadAdapter(getContext(), threadList, this);
        rvThreads.setAdapter(adapter);

        apiService = ApiClient.getClient(getContext()).create(ApiService.class);
        loadThreads();

        return v;
    }

    private void loadThreads() {
        long userId = SessionManager.getInstance(getContext()).getUserId();
        apiService.getUserChatThreads(userId).enqueue(new Callback<List<ChatThread>>() {
            @Override
            public void onResponse(Call<List<ChatThread>> call, Response<List<ChatThread>> response) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    threadList.clear();
                    threadList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ChatThread>> call, Throwable t) {
                if (getActivity() != null) {
                    Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onThreadClick(ChatThread thread) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("user_id", thread.getUserId());
        intent.putExtra("restaurant_id", thread.getRestaurantId());
        intent.putExtra("titleName", thread.getParticipantName());
        startActivity(intent);
    }
}
