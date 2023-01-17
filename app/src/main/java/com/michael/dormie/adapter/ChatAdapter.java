package com.michael.dormie.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.michael.dormie.R;
import com.michael.dormie.model.Chat;
import com.michael.dormie.model.Message;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ItemHolder> {
    private static final String TAG = "ChatAdapter";

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

        if (user == null) return;
        Chat chat = chats.get(position);
        if (chat == null) return;

        List<String> users = chat.getUserList();
        users.remove(user.getUid());
        String receiver = users.get(0);

        FirebaseStorage db = FirebaseStorage.getInstance();
        StorageReference ref = db.getReference().child(receiver + "_avt.jpeg");
        Log.e(TAG, ref.toString());

        List<Message> msg = chat.getMessages();
        Message message = msg.get(msg.size() - 1);
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
