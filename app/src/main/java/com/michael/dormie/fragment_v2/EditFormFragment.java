package com.michael.dormie.fragment_v2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.michael.dormie.R;
import com.michael.dormie.activity.MapsActivity;
import com.michael.dormie.databinding.FragmentEditFormBinding;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.SignalCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditFormFragment extends Fragment {
    private static final String TAG = "EditFormFragment";
    private FragmentEditFormBinding b;
    private IndeterminateDrawable loadIcon;
    private Tenant tenant;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentEditFormBinding.inflate(inflater, container, false);
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
        CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(this.requireContext(), null, 0,
                com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator);
        loadIcon = IndeterminateDrawable.createCircularDrawable(this.requireContext(), spec);
        tenant = new Tenant();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("tenants").document(user.getUid());

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            tenant = documentSnapshot.toObject(Tenant.class);

            for (String s : tenant.getHouseTypes()) {
                if (s.equals("Apartment")) b.apartmentChip.setChecked(true);
                if (s.equals("Villa")) b.villaChip.setChecked(true);
                if (s.equals("House")) b.houseChip.setChecked(true);
                if (s.equals("Townhouse")) b.townhouseChip.setChecked(true);
                if (s.equals("Mobile")) b.mobileChip.setChecked(true);
            }

            for (String s : tenant.getAmenities()) {
                if (s.equals("Washer/Dryer")) b.washerDryerChip.setChecked(true);
                if (s.equals("Ramp access")) b.rampChip.setChecked(true);
                if (s.equals("Garden")) b.gardenChip.setChecked(true);
                if (s.equals("Cats OK")) b.catsOKChip.setChecked(true);
                if (s.equals("Dogs OK")) b.dogsOKChip.setChecked(true);
                if (s.equals("Smoke-free")) b.smokeFreeChip.setChecked(true);
            }

            switch (tenant.getMaxDistance()) {
                case 1000:
                    b.maxDistanceGroup.check(R.id.distBtn5);
                    break;
                case 5000:
                    b.maxDistanceGroup.check(R.id.distBtn6);
                    break;
                case 10000:
                    b.maxDistanceGroup.check(R.id.distBtn7);
                    break;
                case 20000:
                    b.maxDistanceGroup.check(R.id.distBtn8);
                    break;
            }
        });

        addListener();
    }

    private void addListener() {
        b.topAppBar.setNavigationOnClickListener(v -> Navigation.findNavController(b.getRoot()).popBackStack());
        b.savedBtn.setOnClickListener(this::handleSavedBtn);
        b.schoolLayout.setEndIconOnClickListener(this::handleOpenSchoolMap);
        b.schoolEditText.setOnClickListener(this::handleOpenSchoolMap);

        List<Chip> houseChips = Arrays.asList(b.apartmentChip, b.villaChip, b.houseChip,
                b.townhouseChip, b.mobileChip);
        for (Chip chip : houseChips) {
            chip.setOnCheckedChangeListener(this::handleHouseChipCheckedChange);
        }

        List<Chip> amenitiesChips = Arrays.asList(b.washerDryerChip, b.rampChip, b.gardenChip,
                b.catsOKChip, b.dogsOKChip, b.smokeFreeChip);
        for (Chip chip : amenitiesChips) {
            chip.setOnCheckedChangeListener(this::handleAmenitiesChipCheckedChange);
        }

        b.maxDistanceGroup.addOnButtonCheckedListener(this::handleMaxDistanceBtnChecked);
    }

    private void handleMaxDistanceBtnChecked(MaterialButtonToggleGroup materialButtonToggleGroup, int i, boolean b) {
        int maxDist = 0;
        if (b) {
            if (i == this.b.distBtn5.getId()) {
                maxDist = 1000;
                System.out.println(1000);
            } else if (i == this.b.distBtn6.getId()) {
                maxDist = 5000;
                System.out.println(5000);
            } else if (i == this.b.distBtn7.getId()) {
                maxDist = 10000;
                System.out.println(10000);
            } else if (i == this.b.distBtn8.getId()) {
                maxDist = 20000;
            }
            tenant.setMaxDistance(maxDist);
        }
    }

    private void handleHouseChipCheckedChange(CompoundButton compoundButton, boolean b) {
        Log.d(TAG, "Button change: " + compoundButton.getText().toString());
        if (b) {
            tenant.addHouseType(compoundButton.getText().toString());
            return;
        }
        tenant.removeHouseType(compoundButton.getText().toString());
    }

    private void handleAmenitiesChipCheckedChange(CompoundButton compoundButton, boolean b) {
        Log.d(TAG, "Button change: " + compoundButton.getText().toString());
        if (b) {
            tenant.addAmenity(compoundButton.getText().toString());
            return;
        }
        tenant.removeAmenity(compoundButton.getText().toString());
    }

    private void handleOpenSchoolMap(View view) {
        Bundle bundle = new Bundle();
        ArrayList<String> filters = new ArrayList<>();
        filters.add(PlaceTypes.SCHOOL);
        filters.add(PlaceTypes.UNIVERSITY);
        bundle.putStringArrayList(MapsActivity.PARAM_LOCATION_TYPE_FILTER, filters);
        NavigationUtil.navigateActivity(this, this.requireContext(), MapsActivity.class,
                SignalCode.NAVIGATE_MAP, bundle);
    }

    private void handleSavedBtn(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot get user");
            return;
        }
        b.savedBtn.setIcon(loadIcon);
        loadingProcess();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tenants")
                .document(currentUser.getUid())
                .set(tenant, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    b.savedBtn.setIcon(null);
                    completeLoadingProcess();
                })
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "DocumentSnapshot added");
                    Navigation.findNavController(b.getRoot()).popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Snackbar.make(b.getRoot(), "Fail to saved information. Please try again!",
                            Snackbar.LENGTH_LONG).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == SignalCode.NAVIGATE_MAP) {
            Bundle bundle = data.getExtras();
            String locationName = bundle.getString(MapsActivity.PARAM_LOCATION_NAME);
            String locationAddress = bundle.getString(MapsActivity.PARAM_LOCATION_ADDRESS);
            LatLng locationLatLng = (LatLng) bundle.get(MapsActivity.PARAM_LOCATION_LAT_LNG);
            b.schoolEditText.setText(locationName);
            tenant.setSchool(new Tenant.Location(locationName, locationAddress,
                    locationLatLng.latitude, locationLatLng.longitude));
        }
    }

    private void loadingProcess() {
        b.linearProgressIndicator.setVisibility(View.VISIBLE);
        List<View> views = Arrays.asList(b.houseTypesGroup, b.amenitiesGroup,
                b.schoolLayout, b.maxDistanceGroup, b.savedBtn,
                b.washerDryerChip, b.rampChip, b.gardenChip,
                b.catsOKChip, b.dogsOKChip, b.smokeFreeChip,
                b.apartmentChip, b.villaChip, b.houseChip,
                b.townhouseChip, b.mobileChip);
        for (View view : views) {
            view.setEnabled(false);
        }
    }

    private void completeLoadingProcess() {
        b.linearProgressIndicator.setVisibility(View.INVISIBLE);
        List<View> views = Arrays.asList(b.houseTypesGroup, b.amenitiesGroup,
                b.schoolLayout, b.maxDistanceGroup, b.savedBtn,
                b.washerDryerChip, b.rampChip, b.gardenChip,
                b.catsOKChip, b.dogsOKChip, b.smokeFreeChip,
                b.apartmentChip, b.villaChip, b.houseChip,
                b.townhouseChip, b.mobileChip);
        for (View view : views) {
            view.setEnabled(true);
        }
    }
}