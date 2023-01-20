package com.michael.dormie.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.michael.dormie.databinding.ChatBubbleHolderItemBinding;
import com.michael.dormie.model.ChatBubble;
import com.michael.dormie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatBubbleAdapter extends RecyclerView.Adapter<ChatBubbleAdapter.ItemHolder> {
    private static final String TAG = "ChatBubbleAdapter";

    private final List<ChatBubble> chatBubbles;
    private final User receiver;

    public ChatBubbleAdapter(User receiver) {
        chatBubbles = new ArrayList<>();
        this.receiver = receiver;
    }

    public List<ChatBubble> getChatBubbles() {
        return chatBubbles;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatBubbleHolderItemBinding b = ChatBubbleHolderItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        ChatBubble chatBubble = chatBubbles.get(position);
        if (chatBubble == null) return;
        if (chatBubble.getPersonId().equals(receiver.getUid())) {
            Log.e("ChatDetailFragment", chatBubble.getPersonId() + " " + receiver.getUid());
//            Log.e("ChatDetailFragment", String.valueOf(chatBubble.getPersonId().equals(receiver.getUid())));
            Glide.with(holder.itemView).load(receiver.getAvatar()).into(holder.b.avatarImageView);
        } else {
            Glide.with(holder.itemView).clear(holder.b.avatarImageView);
        }

        holder.b.textBody.setText(chatBubble.getContent());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm-MM/dd/yy");
        String date = simpleDateFormat.format(chatBubble.getTimestamp());
        holder.b.timestamp.setText(date);
    }

    @Override
    public int getItemCount() {
        return chatBubbles == null ? 0 : chatBubbles.size();
    }

    public ChatBubble getLatestChatBubble() {
        return chatBubbles == null || chatBubbles.isEmpty() ? null : chatBubbles.get(0);
    }

    public void appendTop(ChatBubble chatBubble) {
        chatBubbles.add(chatBubble);
        notifyItemInserted(chatBubbles.size() - 1);
    }

    public void appendBottom(ChatBubble chatBubble) {
        chatBubbles.add(0, chatBubble);
        notifyItemInserted(0);
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        ChatBubbleHolderItemBinding b;

        public ItemHolder(@NonNull ChatBubbleHolderItemBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

}
