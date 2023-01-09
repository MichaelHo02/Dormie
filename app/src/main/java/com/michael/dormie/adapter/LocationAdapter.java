package com.michael.dormie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.michael.dormie.R;
import com.michael.dormie.implement.IClickableCallback;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ItemHolder> {
    private List<AutocompletePrediction> autocompletePredictions;
    private IClickableCallback handleCardGetClick;

    public LocationAdapter(List<AutocompletePrediction> autocompletePredictions, IClickableCallback handleCardGetClick) {
        this.autocompletePredictions = autocompletePredictions;
        this.handleCardGetClick = handleCardGetClick;
    }

    public void setData(List<AutocompletePrediction> autocompletePredictions) {
        int size = autocompletePredictions.size();
        this.autocompletePredictions = autocompletePredictions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_holder_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        AutocompletePrediction autocompletePrediction = autocompletePredictions.get(position);
        if (autocompletePrediction == null) return;
        holder.name.setText(autocompletePrediction.getPrimaryText(null));
        holder.address.setText(autocompletePrediction.getSecondaryText(null));
        holder.cardView.setOnClickListener(view -> handleCardGetClick.onClickItem(position, autocompletePrediction));
    }

    @Override
    public int getItemCount() {
        return autocompletePredictions != null ? autocompletePredictions.size() : 0;
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        MaterialTextView name, address;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.place_card);
            name = itemView.findViewById(R.id.place_name);
            address = itemView.findViewById(R.id.place_address);
        }
    }

}
