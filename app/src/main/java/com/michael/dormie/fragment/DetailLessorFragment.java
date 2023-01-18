package com.michael.dormie.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;
import com.michael.dormie.adapter.AmenityAdapter;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.databinding.FragmentDetailLessorBinding;
import com.michael.dormie.model.Place;

import com.michael.dormie.utils.FireBaseDBPath;

import java.util.List;

public class DetailLessorFragment extends Fragment {
    private static final String TAG = "DetailFragment";

    private FragmentDetailLessorBinding b;
    private Place place;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentDetailLessorBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        place = DetailTenantFragmentArgs.fromBundle(getArguments()).getPlace();
        b.topAppBar.setNavigationOnClickListener(this::handleNavigationOnClick);
        b.topAppBar.setOnMenuItemClickListener(this::handleMenuClick);

        b.placeName.setText(place.getName());
        b.placeAddress.setText(place.getLocation().address);
        b.placeDescription.setText(place.getDescription());

        PhotoAdapter<String> photoAdapter = new PhotoAdapter<>(requireContext(), place.getImages());
        b.viewPager.setAdapter(photoAdapter);
        b.circleIndicator.setViewPager(b.viewPager);
        photoAdapter.registerAdapterDataObserver(b.circleIndicator.getAdapterDataObserver());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
        List<String> amenities = place.getAmenities();
        AmenityAdapter amenityAdapter = new AmenityAdapter(requireContext(), amenities);
        b.amenities.setLayoutManager(linearLayoutManager);
        b.amenities.setAdapter(amenityAdapter);

        b.editBtn.setOnClickListener(v -> {
            Navigation.findNavController(b.getRoot()).navigate(DetailLessorFragmentDirections
                    .actionDetailLessorFragmentToPlaceCreationFragment(place));
        });
    }

    private boolean handleMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.deletePlace) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(FireBaseDBPath.PROPERTIES).document(place.getUid()).delete().addOnSuccessListener(
                    unused -> Navigation.findNavController(b.getRoot()).popBackStack());
            return true;
        }
        return false;
    }

    private void handleNavigationOnClick(View view) {
        Navigation.findNavController(b.getRoot()).popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}