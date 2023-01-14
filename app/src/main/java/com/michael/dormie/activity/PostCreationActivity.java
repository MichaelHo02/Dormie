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
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.michael.dormie.R;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.model.Place;
import com.michael.dormie.service.PostCreationService;
import com.michael.dormie.utils.DataConverter;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.SignalCode;
import com.michael.dormie.utils.ValidationUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    private TextInputLayout propertyNameLayout, addressLayout, descriptionLayout;
    private MaterialButton submitBtn;
    private Place place = new Place();
    private SubmitResultReceiver receiver = new SubmitResultReceiver(new Handler());

    private boolean isSubmitForm = false;

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

        propertyNameLayout = findViewById(R.id.property_name);
        descriptionLayout = findViewById(R.id.description);

        submitBtn = findViewById(R.id.submit_button);
        submitBtn.setOnClickListener(this::handleSubmitForm);
    }

    private void handleSubmitForm(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ValidationUtil.validateBasic(propertyNameLayout, propertyNameLayout.getEditText().getText().toString());
        ValidationUtil.validateBasic(descriptionLayout, descriptionLayout.getEditText().getText().toString());

        String propertyName = null;
        String description = null;

        if (propertyNameLayout.getError() != null || descriptionLayout.getError() != null) {
            Log.i(TAG, "Input is not passed validation");
            return;
        }

        if (propertyNameLayout.getEditText().getText() != null) {
            propertyName = propertyNameLayout.getEditText().getText().toString();
        }

        if (descriptionLayout.getEditText().getText() != null) {
            description = descriptionLayout.getEditText().getText().toString();
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot get user");
            return;
        }

        place.setAuthorId(currentUser.getUid());
        place.setAuthorRef(db.collection("users").document().getPath());
        place.setName(propertyName);
        place.setDescription(description);

        List<Bitmap> photos = photoAdapter.getPhotos();
        for (Bitmap photo : photos) {
            PostCreationService.startActionUploadImage(
                    this,
                    receiver,
                    String.valueOf(photo.getGenerationId()),
                    DataConverter.convertImageToByteArr(photo));
        }
    }

    private void handleRemovePhoto(View view) {
        photoAdapter.removePhoto(viewPager2.getCurrentItem());
        if (photoAdapter.getItemCount() == 0) {
            imageCover.setVisibility(View.VISIBLE);
        }
    }

    private void handleAddPhoto(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SignalCode.ITEM_CREATION_UPLOAD_PHOTO);
    }

    private List<Bitmap> getPhotos() {
        List<Bitmap> photos = new ArrayList<>();
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
            Bundle bundle = data.getExtras();
            String locationName = bundle.getString(MapsActivity.PARAM_LOCATION_NAME);
            String locationAddress = bundle.getString(MapsActivity.PARAM_LOCATION_ADDRESS);
            LatLng locationLatLng = (LatLng) bundle.get(MapsActivity.PARAM_LOCATION_LAT_LNG);
            addressLayout.getEditText().setText(locationName);
            place.setLocation(new Place.Location(locationName, locationAddress, locationLatLng.latitude,
                    locationLatLng.longitude));
        }
    }

    private class SubmitResultReceiver extends ResultReceiver {
        public SubmitResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.e(TAG, String.valueOf(resultCode));
            if (resultCode == SignalCode.UPLOAD_IMG_SUCCESS) {
                String url = resultData.getString("data");
                place.addImage(url);

                if (place.getImages().size() == photoAdapter.getItemCount() && !isSubmitForm) {
                    PostCreationService.startActionUploadPost(PostCreationActivity.this, receiver, place);
                }
            }

            if (resultCode == SignalCode.UPLOAD_POST_SUCCESS) {
                isSubmitForm = true;
                finish();
            }

            if (resultCode == SignalCode.UPLOAD_POST_ERROR) {
                isSubmitForm = true;
                finish();
            }

            super.onReceiveResult(resultCode, resultData);
        }
    }
}