package com.michael.dormie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michael.dormie.databinding.ChatRoomHolderItemBinding;
import com.michael.dormie.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ItemHolder> {
    List<ChatRoom> chatRooms;
    Map<String, User> userMap;

    public ChatRoomAdapter() {
        this.chatRooms = new ArrayList<>();
        userMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatRoomHolderItemBinding b = ChatRoomHolderItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        ChatRoom chatRoom = chatRooms.get(position);
        if (chatRoom == null) return;
        List<String> userIds = chatRoom.getUserIds();
        userIds.remove(currentUser.getUid());
        String receiverId = userIds.get(0);
        User receiver = userMap.get(receiverId);
        Glide.with(holder.itemView).load(receiver.getAvatar()).into(holder.b.avatarImageView);
        holder.b.nameView.setText(receiver.getName());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void setData(List<ChatRoom> chatRooms, Map<String, User> userMap) {
        this.chatRooms = chatRooms;
        this.userMap = userMap;
        notifyDataSetChanged();
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        ChatRoomHolderItemBinding b;

        public ItemHolder(@NonNull ChatRoomHolderItemBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

}
