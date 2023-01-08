package com.michael.dormie.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.michael.dormie.R;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.model.Place;
import com.michael.dormie.utils.DataConverter;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.SignalCode;
import com.michael.dormie.utils.ValidationUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

        place.authorId = currentUser.getUid();
        place.authorRef = db.collection("users").document();
        place.name = propertyName;
        place.description = description;

        Bitmap[] photos = photoAdapter.getPhotos().toArray(new Bitmap[photoAdapter.getItemCount()]);
//        new UploadImage().execute(photos);

        db.collection("properties")
                .add(place)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Document added");

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference();

                    for (Bitmap photo : photos) {
                        StorageReference postIMGs = storageReference.child(currentUser.getUid() + "_" + photo.getGenerationId() + "_img.jpeg");
                        UploadTask uploadTask = postIMGs.putBytes(DataConverter.convertImageToByteArr(photo));
                        uploadTask.continueWithTask(task -> {
                                    if (!task.isSuccessful()) throw task.getException();
                                    return postIMGs.getDownloadUrl();
                                })
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        place.images.add(task.getResult().toString());
                                        documentReference
                                                .set(place, SetOptions.merge());
//                                        place.images.add(task.getResult().toString());
                                    }
                                });
                    }

                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    finish();
                });
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
            place.location = new Place.Location(locationName, locationAddress, locationLatLng);
        }
    }

    private class UploadImage extends AsyncTask<Bitmap, Integer, Long> {
        protected Long doInBackground(Bitmap... bitmaps) {
            int count = bitmaps.length;
            long totalSize = 0;

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.e(TAG, "Cannot get user");
                return 0L;
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            for (int i = 0; i < count; i++) {
                Bitmap photo = bitmaps[i];
                StorageReference postIMGs = storageReference.child(currentUser.getUid() + "_" + photo.getGenerationId() + "_img.jpeg");
                UploadTask uploadTask = postIMGs.putBytes(DataConverter.convertImageToByteArr(photo));
                uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) throw task.getException();
                            return postIMGs.getDownloadUrl();
                        })
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                place.images.add(task.getResult().toString());
                            }
                        });
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("properties")
                    .add(place)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Document added");
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error adding document", e);
                        finish();
                    });
        }
    }

}