package com.fooddeliveryapp.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fooddeliveryapp.R;
import com.fooddeliveryapp.models.ChatThread;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatThreadAdapter extends RecyclerView.Adapter<ChatThreadAdapter.ViewHolder> {

    private Context context;
    private List<ChatThread> threads;
    private OnThreadClickListener listener;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public interface OnThreadClickListener {
        void onThreadClick(ChatThread thread);
    }

    public ChatThreadAdapter(Context context, List<ChatThread> threads, OnThreadClickListener listener) {
        this.context = context;
        this.threads = threads;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_thread, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatThread thread = threads.get(position);
        holder.tvName.setText(thread.getParticipantName());
        holder.tvMessage.setText(thread.getLastMessage());
        
        if (thread.getLastMessageTime() != null) {
            holder.tvTime.setText(sdf.format(thread.getLastMessageTime()));
        } else {
            holder.tvTime.setText("");
        }

        if (!TextUtils.isEmpty(thread.getParticipantImageUrl())) {
            Glide.with(context).load(thread.getParticipantImageUrl()).into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.mipmap.ic_launcher);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onThreadClick(thread);
        });
    }

    @Override
    public int getItemCount() {
        return threads.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvMessage, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivParticipantRef);
            tvName = itemView.findViewById(R.id.tvParticipantName);
            tvMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvLastMessageTime);
        }
    }
}
