package com.michael.dormie.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.michael.dormie.R;
import com.michael.dormie.implement.ICallBack;
import com.michael.dormie.model.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_LOADING = 2;

    private List<Place> places;
    private Context context;
    private boolean isLoadingAdd;
    private ICallBackItem callBackItem;

    public PlaceAdapter(Context context, List<Place> places, ICallBackItem callBackItem) {
        this.places = places;
        this.context = context;
        this.callBackItem = callBackItem;
    }

    public void setFilteredList(List<Place> filteredList) {
        this.places = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (places != null && position == places.size() - 1 && isLoadingAdd) {
            return TYPE_LOADING;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.place_holder_item, parent, false);
            return new ItemHolder(view);
        } else {
            View view =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_loading, parent, false);
            return new LoadingHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_LOADING) return;
        Place place = places.get(position);
        if (place == null) return;
        ItemHolder itemHolder = (ItemHolder) holder;
        itemHolder.locationName.setText(place.getName());
        if (place.getLocation() == null) return;
        String addressDisplay = "Address: " + place.getLocation().address;
        itemHolder.locationAddress.setText(addressDisplay);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.sample)
                .error(R.drawable.sample);
        Glide.with(context).load(place.getImages().get(0)).apply(options).into(itemHolder.locationImage);
        itemHolder.root.setOnClickListener(v -> callBackItem.onClickItem(place));
    }

    @Override
    public int getItemCount() {
        return places == null ? 0 : places.size();
    }

    public void addFooterLoading() {
        isLoadingAdd = true;
//        places.add(null);
    }

    public void removeFooterLoading() {
        isLoadingAdd = false;
        int idx = places.size() - 1;
//        places.remove(idx);
        notifyItemRemoved(idx);
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

    public static final class LoadingHolder extends RecyclerView.ViewHolder {
        CircularProgressIndicator indicator;

        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
            indicator = itemView.findViewById(R.id.circleIndicator);
        }
    }

    public interface ICallBackItem {
        void onClickItem(Place place);
    }
}
