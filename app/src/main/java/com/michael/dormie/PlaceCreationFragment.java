package com.michael.dormie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.michael.dormie.activity.MapsActivity;
import com.michael.dormie.activity.PostCreationActivity;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.databinding.FragmentPlaceCreationBinding;
import com.michael.dormie.model.Place;
import com.michael.dormie.service.PostCreationService;
import com.michael.dormie.utils.SignalCode;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PlaceCreationFragment extends Fragment {
    private static final String TAG = "PostCreationActivity";

    private MaterialToolbar topAppBar;
    private PhotoAdapter photoAdapter;
    private Place place = new Place();
    private PlaceCreationFragment.SubmitResultReceiver receiver =
            new SubmitResultReceiver(new Handler());
    FragmentPlaceCreationBinding b;
    private boolean isSubmitForm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentPlaceCreationBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isSubmitForm = false;

        b.topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        photoAdapter = new PhotoAdapter(this.requireContext(), getPhotos());
        b.viewPager.setAdapter(photoAdapter);
        b.circleIndicator.setViewPager(b.viewPager);
        photoAdapter.registerAdapterDataObserver(b.circleIndicator.getAdapterDataObserver());

        b.uploadBtn.setOnClickListener(this::handleAddPhoto);
        b.removeBtn.setOnClickListener(this::handleRemovePhoto);

        b.addressLayout.setEndIconOnClickListener(this::handleOpenMap);
        b.addressEditText.setOnClickListener(this::handleOpenMap);

        b.submitBtn.setOnClickListener(this::handleSubmitForm);
    }

    private List<Bitmap> getPhotos() {
        List<Bitmap> photos = new ArrayList<>();
        return photos;
    }

    private void handleAddPhoto(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SignalCode.ITEM_CREATION_UPLOAD_PHOTO);
    }

    private void handleRemovePhoto(View view) {
        photoAdapter.removePhoto(b.viewPager.getCurrentItem());
        if (photoAdapter.getItemCount() == 0) {
            b.imageCover.setVisibility(View.VISIBLE);
        }
    }

    private void handleOpenMap(View view) {

    }

    private void handleSubmitForm(View view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == SignalCode.ITEM_CREATION_UPLOAD_PHOTO) {
            Log.d(TAG, "Put image taken from library");
            Uri selectedImg = data.getData();

            InputStream imageStream = null;
            try {
                imageStream =
                        this.requireActivity().getContentResolver().openInputStream(selectedImg);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            photoAdapter.addPhoto(bitmap);
            b.viewPager.setCurrentItem(photoAdapter.getItemCount());
            b.imageCover.setVisibility(View.GONE);
        }

        if (requestCode == SignalCode.NAVIGATE_MAP) {
            Bundle bundle = data.getExtras();
            String locationName = bundle.getString(MapsActivity.PARAM_LOCATION_NAME);
            String locationAddress = bundle.getString(MapsActivity.PARAM_LOCATION_ADDRESS);
            LatLng locationLatLng = (LatLng) bundle.get(MapsActivity.PARAM_LOCATION_LAT_LNG);
            b.addressEditText.setText(locationName);
            place.location = new Place.Location(locationName, locationAddress, locationLatLng.latitude, locationLatLng.longitude);
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
                place.images.add(url);

                if (place.images.size() == photoAdapter.getItemCount() && !isSubmitForm) {
//                    PostCreationService.startActionUploadPost(PostCreationActivity.this, receiver, place);
                }
            }

            if (resultCode == SignalCode.UPLOAD_POST_SUCCESS) {
                isSubmitForm = true;
//                finish();
            }

            if (resultCode == SignalCode.UPLOAD_POST_ERROR) {
                isSubmitForm = true;
//                finish();
            }

            super.onReceiveResult(resultCode, resultData);
        }
    }
}