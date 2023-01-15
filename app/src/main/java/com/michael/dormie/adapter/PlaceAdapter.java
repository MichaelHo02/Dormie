package com.michael.dormie.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.michael.dormie.R;
import com.michael.dormie.fragment.DetailFragment;
import com.michael.dormie.fragment_v2.HomeLessorFragment;
import com.michael.dormie.fragment_v2.HomeLessorFragmentDirections;
import com.michael.dormie.model.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ItemHolder> {
    private List<Place> places;
    private Context context;

    public PlaceAdapter(Context context, List<Place> places) {
        this.places = places;
        this.context = context;
    }

    public void setFilteredList(List<Place> filteredList) {
        this.places = filteredList;
        notifyDataSetChanged();
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
        Place place = places.get(position);
        if (place == null) return;
        Log.e("ABC", "HElO");
        holder.locationName.setText(place.getName());
        String addressDisplay = "Address: " + place.getLocation().address;
        holder.locationAddress.setText(addressDisplay);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.sample)
                .error(R.drawable.sample);
        Glide.with(context).load(place.getImages().get(0)).apply(options).into(holder.locationImage);
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("place_detail", place);
                Navigation.findNavController(holder.itemView).navigate(R.id.action_homeLessorFragment_to_detailFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places != null ? places.size() : 0;
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        View root;
        ShapeableImageView locationImage;
        MaterialTextView locationName, locationAddress;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView;
            locationImage = itemView.findViewById(R.id.locationImage);
            locationName = itemView.findViewById(R.id.locationName);
            locationAddress = itemView.findViewById(R.id.locationAddress);
        }
    }
}
