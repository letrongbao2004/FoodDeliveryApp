package com.fooddeliveryapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.ChatMessage;
import com.fooddeliveryapp.utils.SessionManager;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private static final int TYPE_SYSTEM = 3;

    private List<ChatMessage> messages;
    private Context context;
    private String currentUserRole;

    public ChatMessageAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
        this.currentUserRole = SessionManager.getInstance(context).getRole().toUpperCase();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if ("SYSTEM".equals(msg.getSenderRole())) {
            return TYPE_SYSTEM;
        } else if (currentUserRole.equals(msg.getSenderRole())) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_sent, parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == TYPE_RECEIVED) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_received, parent, false);
            return new MessageViewHolder(view);
        } else {
            // SYSTEM message uses same received layout but centered and gray
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message_received, parent, false);
            return new SystemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).tvContent.setText(msg.getContent());
        } else if (holder instanceof SystemViewHolder) {
            ((SystemViewHolder) holder).tvContent.setText(msg.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;

        MessageViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
        }
    }

    static class SystemViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;

        SystemViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvContent.setBackgroundResource(android.R.color.transparent);
            tvContent.setTextColor(Color.GRAY);
            // Center in parent
            ((View) tvContent.getParent()).setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            ((android.widget.LinearLayout) tvContent.getParent()).setGravity(android.view.Gravity.CENTER);
        }
    }
}
