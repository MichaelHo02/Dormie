package com.michael.dormie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michael.dormie.R;
import com.michael.dormie.model.Chat;
import com.michael.dormie.model.Message;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ItemHolder>{
    private Context context;
    private List<Chat> chats;

    public ChatAdapter(Context context, List<Chat> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public ChatAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_holder_item, parent, false);
        return new ChatAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ItemHolder holder, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Chat chat = chats.get(position);
        if (chat == null) return;
        // add value
        List<String> users = chat.getUserIds();
        String receiverId = null;
        for (String u : users) {
            if (u != user.getUid()) {
                holder.receiver.setText(u); // Need to recall the user information -> Create user class
            }
        }
        List<Message> msg = chat.getMessages();
        Message message = msg.get(msg.size() -1);
        // photoId - lack of
        holder.lastMsg.setText(message.getContent());
        holder.time.setText(String.valueOf(message.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public final static class ItemHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView profile_img;
        private MaterialTextView receiver, lastMsg, time;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            profile_img = itemView.findViewById(R.id.chat_profile_image);
            receiver = itemView.findViewById(R.id.reiver_name);
            lastMsg = itemView.findViewById(R.id.last_msg);
            time = itemView.findViewById(R.id.chat_time);
        }
    }
}
