package com.fooddeliveryapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.adapters.ChatMessageAdapter;
import com.fooddeliveryapp.models.ChatMessage;
import com.fooddeliveryapp.remote.ApiClient;
import com.fooddeliveryapp.remote.ApiService;
import com.fooddeliveryapp.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private EditText etInput;
    private ImageView btnSend;

    private Long userId;
    private Long restaurantId;
    private String titleName;

    private ChatMessageAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private ApiService apiService;

    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userId = getIntent().getLongExtra("user_id", -1L);
        restaurantId = getIntent().getLongExtra("restaurant_id", -1L);
        titleName = getIntent().getStringExtra("titleName");

        if (userId == -1L || restaurantId == -1L) {
            Toast.makeText(this, "Invalid chat parameters", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(titleName != null ? titleName : "Chat");
        }

        rvHistory = findViewById(R.id.rvChatHistory);
        etInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSendMessage);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvHistory.setLayoutManager(lm);
        adapter = new ChatMessageAdapter(this, messages);
        rvHistory.setAdapter(adapter);

        apiService = ApiClient.getClient(this).create(ApiService.class);
        compositeDisposable = new CompositeDisposable();

        btnSend.setOnClickListener(v -> sendMessage());

        loadHistory();
        initStomp();
    }

    private void loadHistory() {
        apiService.getChatHistory(userId, restaurantId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if(isFinishing()) return;
                if(response.isSuccessful() && response.body() != null) {
                    messages.clear();
                    messages.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        rvHistory.scrollToPosition(messages.size() - 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                if(!isFinishing()) {
                    Toast.makeText(ChatActivity.this, "Could not load history", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("CheckResult")
    private void initStomp() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, ApiClient.getWebSocketUrl());
        
        compositeDisposable.add(stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.i("ChatActivity", "Stomp connection opened");
                    subscribeToTopic();
                    break;
                case ERROR:
                    Log.e("ChatActivity", "Error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.i("ChatActivity", "Stomp connection closed");
                    break;
            }
        }));

        stompClient.connect();
    }

    @SuppressLint("CheckResult")
    private void subscribeToTopic() {
        String topic = "/topic/chat/" + userId + "_" + restaurantId;
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        
        compositeDisposable.add(stompClient.topic(topic).subscribe(topicMessage -> {
            ChatMessage msg = gson.fromJson(topicMessage.getPayload(), ChatMessage.class);
            runOnUiThread(() -> {
                messages.add(msg);
                adapter.notifyItemInserted(messages.size() - 1);
                rvHistory.scrollToPosition(messages.size() - 1);
            });
        }, throwable -> {
            Log.e("ChatActivity", "Error on subscribe topic", throwable);
        }));
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        ChatMessage msg = new ChatMessage();
        msg.setUserId(userId);
        msg.setRestaurantId(restaurantId);
        msg.setContent(text);
        msg.setSenderRole(SessionManager.getInstance(this).getRole().toUpperCase());

        String payload = new Gson().toJson(msg);
        stompClient.send("/app/chat.send", payload).subscribe(() -> {
            runOnUiThread(() -> etInput.setText(""));
        }, throwable -> {
            Log.e("ChatActivity", "Error sending message", throwable);
            runOnUiThread(() -> Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }
}
