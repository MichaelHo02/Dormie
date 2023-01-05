package com.michael.dormie.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.michael.dormie.R;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.model.Photo;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.SignalCode;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator3;

public class PostCreationActivity extends AppCompatActivity {
    private static final String TAG = "PostCreationActivity";

    private MaterialToolbar topAppBar;
    private ViewPager2 viewPager2;
    private CircleIndicator3 circleIndicator3;
    private PhotoAdapter photoAdapter;
    private MaterialButton addPhoto;
    private MaterialButton removePhoto;
    private LinearLayout imageCover;

    private TextInputLayout addressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creation);
        initUI();
    }

    private void initUI() {
        topAppBar = findViewById(R.id.top_bar);
        topAppBar.setNavigationOnClickListener(view -> finish());
        imageCover = findViewById(R.id.image_cover);
        photoAdapter = new PhotoAdapter(this, getPhotos());
        viewPager2 = findViewById(R.id.view_pager);
        viewPager2.setAdapter(photoAdapter);
        circleIndicator3 = findViewById(R.id.circle_indicator);
        circleIndicator3.setViewPager(viewPager2);
        photoAdapter.registerAdapterDataObserver(circleIndicator3.getAdapterDataObserver());

        addPhoto = findViewById(R.id.btn_upload);
        addPhoto.setOnClickListener(this::handleAddPhoto);

        removePhoto = findViewById(R.id.btn_remove);
        removePhoto.setOnClickListener(this::handleRemovePhoto);

        addressLayout = findViewById(R.id.address);
        addressLayout.setEndIconOnClickListener(view ->
                NavigationUtil.navigateActivity(
                        PostCreationActivity.this,
                        PostCreationActivity.this.getBaseContext(),
                        MapsActivity.class, SignalCode.NAVIGATE_MAP));
    }

    private void handleRemovePhoto(View view) {
        photoAdapter.removePhoto(viewPager2.getCurrentItem());
        if (photoAdapter.getItemCount() == 0) {
            imageCover.setVisibility(View.VISIBLE);
        }
    }

    private void handleAddPhoto(View view) {
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//        startActivityForResult(intent, SignalCode.ITEM_CREATION_UPLOAD_PHOTO);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SignalCode.ITEM_CREATION_UPLOAD_PHOTO);
    }

    private List<Photo> getPhotos() {
        List<Photo> photos = new ArrayList<>();
        return photos;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == SignalCode.ITEM_CREATION_UPLOAD_PHOTO) {
            Log.d(TAG, "Put image taken from library");
            Uri selectedImg = data.getData();

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(selectedImg);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            photoAdapter.addPhoto(bitmap);
            viewPager2.setCurrentItem(photoAdapter.getItemCount());
            imageCover.setVisibility(View.GONE);
        }

        if (requestCode == SignalCode.NAVIGATE_MAP) {
            String address = data.getStringExtra("address");
            addressLayout.getEditText().setText(address);
        }
    }
}