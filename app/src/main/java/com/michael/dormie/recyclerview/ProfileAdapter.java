package com.michael.dormie.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.michael.dormie.R;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private final ProfileRecyclerInterface recyclerInterface;
    private List<ProfileCard> list;

    public ProfileAdapter(ProfileRecyclerInterface recyclerInterface, List<ProfileCard> list) {
        this.recyclerInterface = recyclerInterface;
        this.list = list;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_card, parent, false);
        return new ProfileViewHolder(view, recyclerInterface);
    }


    public void setData (List<ProfileCard> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ProfileViewHolder holder, int position) {
        ProfileCard card = list.get(position);
        if (card == null) {
            return;
        }
        holder.cardTitle.setText(card.getCardTitle());
        holder.cardContent.setText(card.getCardContent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder{

        private TextView cardTitle;
        private TextView cardContent;

        public ProfileViewHolder(@NonNull View itemView, ProfileRecyclerInterface recyclerInterface) {
            super(itemView);

            cardTitle = itemView.findViewById(R.id.card_title);
            cardContent = itemView.findViewById(R.id.card_content);
        }
    }
}
