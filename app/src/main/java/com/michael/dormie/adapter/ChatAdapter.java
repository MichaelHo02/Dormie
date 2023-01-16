package com.michael.dormie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michael.dormie.R;
import com.michael.dormie.implement.ChatListener;
import com.michael.dormie.model.Chat;
import com.michael.dormie.model.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.SimpleTimeZone;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ItemHolder> {
    private List<Chat> chats;
    private final ChatListener listener;
    private FirebaseUser user;
//    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    public ChatAdapter(List<Chat> chats, ChatListener listener) {
        this.chats = chats;
        this.listener = listener;
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
        user = FirebaseAuth.getInstance().getCurrentUser();
        Chat chat = chats.get(position);
        Message msg = chat.getContent().get(-1);
        if (chat == null) return;
        holder.contact.setText(chat.getSentId());
        holder.lastMsg.setText(msg.getContent());
        holder.time.setText(msg.getTimestamp());

    }

    @Override
    public int getItemCount() {
        return chats == null ? 0 : chats.size();
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder{
        MaterialTextView contact, lastMsg, time;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            contact = itemView.findViewById(R.id.chat_contact);
            lastMsg = itemView.findViewById(R.id.last_msg);
            time = itemView.findViewById(R.id.chat_time);
        }
    }
}
