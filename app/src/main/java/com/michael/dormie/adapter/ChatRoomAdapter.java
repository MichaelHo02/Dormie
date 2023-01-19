package com.michael.dormie.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michael.dormie.databinding.ChatRoomHolderItemBinding;
import com.michael.dormie.fragment.ChatFragmentDirections;
import com.michael.dormie.model.ChatRoom;
import com.michael.dormie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ItemHolder> {
    private static final String TAG = "ChatRoomAdapter";

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
        holder.b.getRoot().setOnClickListener(v -> Navigation.findNavController(holder.itemView)
                .navigate(ChatFragmentDirections
                        .actionChatFragmentToChatDetailFragment(receiver, chatRoom)));

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm-MM/dd");
            String date = simpleDateFormat.format(chatRoom.getTimeStamp());
            String lastMsg = chatRoom.getLatestMessageSender() + ": " + chatRoom.getLatestMessage();
            holder.b.lastMsgView.setText(lastMsg);
            holder.b.timeLastMsgView.setText(date);
        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return chatRooms == null ? 0 : chatRooms.size();
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
