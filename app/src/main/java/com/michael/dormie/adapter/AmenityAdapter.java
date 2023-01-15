package com.michael.dormie.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.michael.dormie.R;

import java.util.List;

public class AmenityAdapter extends RecyclerView.Adapter<AmenityAdapter.ItemHolder>{
    private List<String> amenities;
    private Context context;

    public AmenityAdapter(Context context, List<String> amenities) {
        this.amenities = amenities;
        this.context = context;
    }

    @NonNull
    @Override
    public AmenityAdapter.ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.amenity_holder_item, parent, false);
        return new AmenityAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenityAdapter.ItemHolder holder, int position) {
        String amenity = amenities.get(position);
        if (amenity == null) return;
        holder.chip.setText(amenity);
    }

    @Override
    public int getItemCount() {
        return amenities != null ? amenities.size() : 0;
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        Chip chip;
        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.dt_amenity_chip);
        }
    }
}
