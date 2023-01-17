package com.michael.dormie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.michael.dormie.R;
import com.michael.dormie.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ItemHolder>{
    private Context context;
    private List<Message> messages;

    public MessageAdapter(Context context) {
        this.context = context;
        this.messages = new ArrayList<>();
    }

    public void add(Message message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_holder_item, parent, false);
        return new MessageAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ItemHolder holder, int position) {
        Message message = messages.get(position);
        if (message == null) return;
        holder.msg.setText(message.getContent());

        if (message.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            holder.msg.setTextColor(context.getResources().getColor(com.stripe.android.payments.R.color.places_text_black_alpha_26));
            holder.layout.setBackgroundColor(context.getResources().getColor(com.stripe.android.R.color.material_dynamic_neutral30));
        } else {
            holder.msg.setTextColor(context.getResources().getColor(com.stripe.android.R.color.material_dynamic_neutral30));
            holder.layout.setBackgroundColor(context.getResources().getColor(com.stripe.android.payments.R.color.places_text_black_alpha_26));
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder{
        MaterialTextView msg;
        LinearLayout layout;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            msg = itemView.findViewById(R.id.msg_content);
            layout = itemView.findViewById(R.id.msg_layout);
        }
    }
}
