package com.michael.dormie.recyclerview;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.michael.dormie.R;

import java.util.List;

public class RentalRegistrationAdapter extends RecyclerView.Adapter<RentalRegistrationAdapter.RentalRegistrationViewHolder>{

    private RentalRegistrationRecyclerInterface recyclerInterface;
    private List<RentalRegistrationCard> list;

    @NonNull
    @Override
    public RentalRegistrationAdapter.RentalRegistrationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_card, parent, false);
        return new RentalRegistrationAdapter.RentalRegistrationViewHolder(view, recyclerInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RentalRegistrationAdapter.RentalRegistrationViewHolder holder, int position) {
        RentalRegistrationCard card = list.get(position);
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

    public class RentalRegistrationViewHolder extends RecyclerView.ViewHolder {
        private TextView cardTitle;
        private TextView cardContent;
        private MaterialButton cardButton;

        public RentalRegistrationViewHolder(@NonNull View itemView, RentalRegistrationRecyclerInterface recyclerInterface) {
            super(itemView);

            cardTitle = itemView.findViewById(R.id.registration_title);
            cardContent = itemView.findViewById(R.id.registration_content);
            cardButton = itemView.findViewById(R.id.registration_check);

            cardButton.addOnCheckedChangeListener(this::handleRegistrationCheckClick);
        }

        private void handleRegistrationCheckClick(MaterialButton materialButton, boolean isChecked) {

        }


    }
}
