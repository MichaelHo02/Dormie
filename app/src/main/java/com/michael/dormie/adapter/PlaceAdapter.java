package com.michael.dormie.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.michael.dormie.R;
import com.michael.dormie.model.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ItemHolder> {
    private final List<Place> places;
    private final Context context;

    public PlaceAdapter(Context context, List<Place> places) {
        this.places = places;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_holder_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.locationName.setText(places.get(position).name);
        String addressDisplay = "Address: " + places.get(position).location.address;
        holder.locationAddress.setText(addressDisplay);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.sample)
                .error(R.drawable.sample);

        Glide.with(context).load(places.get(position).images.get(0)).apply(options).into(holder.locationImage);
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        ShapeableImageView locationImage;
        MaterialTextView locationName, locationAddress;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            locationImage = itemView.findViewById(R.id.locationImage);
            locationName = itemView.findViewById(R.id.locationName);
            locationAddress = itemView.findViewById(R.id.locationAddress);
        }
    }
}
