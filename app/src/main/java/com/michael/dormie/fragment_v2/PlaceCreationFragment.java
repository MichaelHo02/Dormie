package com.michael.dormie.fragment_v2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.activity.MapsActivity;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.databinding.FragmentPlaceCreationBinding;
import com.michael.dormie.model.Place;
import com.michael.dormie.service.PostCreationService;
import com.michael.dormie.utils.DataConverter;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.SignalCode;
import com.michael.dormie.utils.ValidationUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlaceCreationFragment extends Fragment {
    private static final String TAG = "PostCreationActivity";

    private PhotoAdapter<PhotoAdapter.PhotoObject> photoAdapter;
    private Place place = new Place();
    private final PlaceCreationFragment.SubmitResultReceiver receiver =
            new SubmitResultReceiver(new Handler());
    private FragmentPlaceCreationBinding b;
    private boolean isSubmitForm;
    private IndeterminateDrawable loadIcon;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(this.requireContext(), null, 0,
                com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator);
        loadIcon = IndeterminateDrawable.createCircularDrawable(this.requireContext(), spec);


        Place p = PlaceCreationFragmentArgs.fromBundle(getArguments()).getPlace();
        if (p != null) {
            this.place = p;
            List<PhotoAdapter.PhotoObject> photoObjects = new ArrayList<>();
            for (String image : place.getImages()) {
                photoObjects.add(new PhotoAdapter.PhotoObject(image));
            }
            photoAdapter = new PhotoAdapter<>(requireContext(), photoObjects);
            b.imageCover.setVisibility(View.GONE);

            b.nameEditText.setText(place.getName());
            b.addressEditText.setText(place.getLocation().name);
            b.descriptionEditText.setText(place.getDescription());

            List<Chip> houseChips = Arrays.asList(
                    b.apartmentChip, b.villaChip, b.houseChip,
                    b.townhouseChip, b.mobileChip
            );
            for (Chip houseChip : houseChips) {
                if (houseChip.getText().toString().equals(place.getHouseType())) {
                    houseChip.setChecked(true);
                    break;
                }
            }

            List<Chip> amenitiesChips = Arrays.asList(b.washerDryerChip, b.rampChip, b.gardenChip,
                    b.catsOKChip, b.dogsOKChip, b.smokeFreeChip);
            for (Chip amenitiesChip : amenitiesChips) {
                if (place.getAmenities().contains(amenitiesChip.getText().toString())) {
                    amenitiesChip.setChecked(true);
                }
            }
        } else {
            photoAdapter = new PhotoAdapter<>(this.requireContext(), new ArrayList<>());
        }

        b.topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        b.viewPager.setAdapter(photoAdapter);
        b.circleIndicator.setViewPager(b.viewPager);
        photoAdapter.registerAdapterDataObserver(b.circleIndicator.getAdapterDataObserver());

        b.uploadBtn.setOnClickListener(this::handleAddPhoto);
        b.removeBtn.setOnClickListener(this::handleRemovePhoto);

        b.addressLayout.setEndIconOnClickListener(this::handleOpenMap);
        b.addressEditText.setOnClickListener(this::handleOpenMap);

        b.houseTypesGroup.setOnCheckedStateChangeListener(this::handleHouseChipCheckedChange);

        List<Chip> amenitiesChips = Arrays.asList(b.washerDryerChip, b.rampChip, b.gardenChip,
                b.catsOKChip, b.dogsOKChip, b.smokeFreeChip);
        for (Chip chip : amenitiesChips) {
            chip.setOnCheckedChangeListener(this::handleAmenitiesChipCheckedChange);
        }

        b.submitBtn.setOnClickListener(this::handleSubmitForm);
    }

    private void handleHouseChipCheckedChange(ChipGroup chipGroup, List<Integer> integers) {
        Chip c = b.getRoot().findViewById(chipGroup.getCheckedChipId());
        place.setHouseType(c.getText().toString());
        Log.d(TAG, c.getText().toString());
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
        Bundle bundle = new Bundle();
        ArrayList<String> filters = new ArrayList<>();
        filters.add(PlaceTypes.GEOCODE);
        bundle.putStringArrayList(MapsActivity.PARAM_LOCATION_TYPE_FILTER, filters);
        NavigationUtil.navigateActivity(
                this,
                this.requireContext(),
                MapsActivity.class, SignalCode.NAVIGATE_MAP, bundle);
    }


    private void handleAmenitiesChipCheckedChange(CompoundButton compoundButton, boolean b) {
        Log.d(TAG, "Button change: " + compoundButton.getText().toString());
        if (b) {
            place.addAmenity(compoundButton.getText().toString());
            return;
        }
        place.removeAmenity(compoundButton.getText().toString());
    }

    private void handleSubmitForm(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ValidationUtil.validateBasic(b.nameLayout, b.nameEditText.getText().toString());
        ValidationUtil.validateBasic(b.descriptionLayout, b.descriptionEditText.getText().toString());

        String propertyName;
        String description;

        String errorMsg = "";
        if (b.nameLayout.getError() != null)
            errorMsg += "Invalid property name. ";
        if (b.descriptionLayout.getError() != null)
            errorMsg += "Invalid description. ";
        if (photoAdapter.getItemCount() == 0)
            errorMsg += "Invalid or no photo.";

        if (b.nameLayout.getError() != null || b.descriptionLayout.getError() != null || photoAdapter.getItemCount() == 0) {
            Log.i(TAG, "Input is not passed validation");
            Snackbar.make(b.getRoot(), errorMsg, Snackbar.LENGTH_LONG).show();
            return;
        }

        propertyName = b.nameEditText.getText().toString();
        description = b.descriptionEditText.getText().toString();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot get user");
            return;
        }

        place.setAuthorId(currentUser.getUid());
        place.setAuthorRef(db.collection("users").document().getPath());
        place.setName(propertyName);
        place.setDescription(description);

        b.submitBtn.setIcon(loadIcon);
        loadingProcess();

        List<PhotoAdapter.PhotoObject> photos = photoAdapter.getPhotos();
        boolean needUploadPhoto = false;
        for (PhotoAdapter.PhotoObject photo : photos) {
            if (photo.bitmap != null) {
                needUploadPhoto = true;
                Bitmap bitmap = photo.bitmap;
                PostCreationService.startActionUploadImage(
                        this.requireContext(),
                        receiver,
                        String.valueOf(bitmap.getGenerationId()),
                        DataConverter.convertImageToByteArr(bitmap));
            }
        }
        if (!needUploadPhoto) handleSavePost();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, requestCode + " " + resultCode);
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
            photoAdapter.addPhoto(new PhotoAdapter.PhotoObject(bitmap));
            b.viewPager.setCurrentItem(photoAdapter.getItemCount());
            b.imageCover.setVisibility(View.GONE);
        }

        if (requestCode == SignalCode.NAVIGATE_MAP) {
            Bundle bundle = data.getExtras();
            String locationName = bundle.getString(MapsActivity.PARAM_LOCATION_NAME);
            String locationAddress = bundle.getString(MapsActivity.PARAM_LOCATION_ADDRESS);
            LatLng locationLatLng = (LatLng) bundle.get(MapsActivity.PARAM_LOCATION_LAT_LNG);
            Log.e(TAG, locationAddress);
            b.addressEditText.setText(locationName);
            String hash =
                    GeoFireUtils.getGeoHashForLocation(new GeoLocation(locationLatLng.latitude,
                            locationLatLng.longitude));
            place.setLocation(new Place.Location(locationName, locationAddress,
                    locationLatLng.latitude, locationLatLng.longitude, hash));
        }
    }

    private class SubmitResultReceiver extends ResultReceiver {
        public SubmitResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            Log.e(TAG, String.valueOf(resultCode));
            if (resultCode == SignalCode.UPLOAD_IMG_SUCCESS) {
                String url = resultData.getString("data");
                place.addImage(url);

                if (place.getImages().size() == photoAdapter.getItemCount() && !isSubmitForm) {
                    handleSavePost();
                }
            }

            if (resultCode == SignalCode.UPLOAD_POST_SUCCESS) {
                isSubmitForm = true;
                b.submitBtn.setIcon(null);
                completeLoadingProcess();
                Navigation.findNavController(b.getRoot()).popBackStack();
            }

            if (resultCode == SignalCode.UPLOAD_POST_ERROR) {
                isSubmitForm = true;
                b.submitBtn.setIcon(null);
                completeLoadingProcess();
                Navigation.findNavController(b.getRoot()).popBackStack();
            }
        }
    }

    private void handleSavePost() {
        if (place.getUid() == null || place.getUid().isEmpty())
            place.setUid(UUID.randomUUID().toString());
        PostCreationService.startActionUploadPost(
                PlaceCreationFragment.this.requireContext(),
                receiver,
                place);
    }

    private void loadingProcess() {
        b.linearProgressIndicator.setVisibility(View.VISIBLE);
        List<View> views = Arrays.asList(b.removeBtn, b.uploadBtn, b.nameLayout,
                b.addressLayout, b.descriptionLayout,
                b.houseTypesGroup, b.amenitiesGroup, b.submitBtn,
                b.washerDryerChip, b.rampChip, b.gardenChip,
                b.catsOKChip, b.dogsOKChip, b.smokeFreeChip,
                b.apartmentChip, b.villaChip, b.houseChip,
                b.townhouseChip, b.mobileChip
        );
        for (View view : views) {
            view.setEnabled(false);
        }
    }

    private void completeLoadingProcess() {
        b.linearProgressIndicator.setVisibility(View.INVISIBLE);
        List<View> views = Arrays.asList(b.removeBtn, b.uploadBtn, b.nameLayout,
                b.addressLayout, b.descriptionLayout,
                b.houseTypesGroup, b.amenitiesGroup, b.submitBtn,
                b.washerDryerChip, b.rampChip, b.gardenChip,
                b.catsOKChip, b.dogsOKChip, b.smokeFreeChip,
                b.apartmentChip, b.villaChip, b.houseChip,
                b.townhouseChip, b.mobileChip
        );
        for (View view : views) {
            view.setEnabled(true);
        }
    }
}