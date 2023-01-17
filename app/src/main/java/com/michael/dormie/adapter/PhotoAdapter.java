package com.michael.dormie.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.michael.dormie.R;

import java.util.List;

public class PhotoAdapter<T> extends RecyclerView.Adapter<PhotoAdapter.ItemHolder> {
    private static final String TAG = "PhotoAdapter";

    private Context context;
    private List<T> photos;

    public PhotoAdapter(Context context, List<T> photos) {
        this.context = context;
        this.photos = photos;
    }

    public List<T> getPhotos() {
        return photos;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_holder_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        T photo = photos.get(position);
        if (photo == null) return;
        if (photo instanceof PhotoObject) {
            Log.e(TAG, "Object instance of PhotoObject");
            PhotoObject photoObject = (PhotoObject) photo;
            if (photoObject.url != null) {
                Glide.with(context).load(photoObject.url).into(holder.imageView);
            } else {
                Glide.with(context).load(photoObject.bitmap).into(holder.imageView);
            }
        } else {
            Log.e(TAG, "Any other Object");
            Glide.with(context).load((String) photo).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    public void addPhoto(T bitmap) {
        photos.add(bitmap);
        notifyItemInserted(photos.size() + 1);
    }

    public void removePhoto(int position) {
        photos.remove(position);
        notifyItemRemoved(position);
    }

    public static final class ItemHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imageView;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_photo);
        }
    }

    public static final class PhotoObject {
        public String url;
        public Bitmap bitmap;

        public PhotoObject(String image) {
            this.url = image;
        }

        public PhotoObject(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}
