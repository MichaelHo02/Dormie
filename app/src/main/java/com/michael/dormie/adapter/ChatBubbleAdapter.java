package com.michael.dormie.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michael.dormie.databinding.ChatBubbleHolderItemBinding;
import com.michael.dormie.model.ChatBubble;
import com.michael.dormie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatBubbleAdapter extends RecyclerView.Adapter<ChatBubbleAdapter.ItemHolder> {
    private static final String TAG = "ChatBubbleAdapter";

    List<ChatBubble> chatBubbles;
    User receiver;

    public ChatBubbleAdapter(User receiver) {
        chatBubbles = new ArrayList<>();
        this.receiver = receiver;
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        ChatBubble chatBubble = chatBubbles.get(position);
        if (chatBubble == null) return;
        if (!Objects.equals(chatBubble.getPersonId(), currentUser.getUid())) {
            Glide.with(holder.itemView).load(receiver.getAvatar()).into(holder.b.avatarImageView);
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

    public void setData(List<ChatBubble> chatBubbles) {
        this.chatBubbles.addAll(chatBubbles);
        notifyDataSetChanged();
    }

    public void addData(ChatBubble chatBubble) {
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
