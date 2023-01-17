package com.michael.dormie.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;
import com.michael.dormie.adapter.AmenityAdapter;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.databinding.FragmentDetailTenantBinding;
import com.michael.dormie.model.Place;

import java.util.ArrayList;
import java.util.List;

public class DetailTenantFragment extends Fragment {

    FragmentDetailTenantBinding b;

    private PhotoAdapter<String> photoAdapter;
    private List<String> photos;
    private AmenityAdapter amenityAdapter;
    private List<String> amenities;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentDetailTenantBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Place place = DetailTenantFragmentArgs.fromBundle(getArguments()).getPlace();
        b.topAppBar.setNavigationOnClickListener(this::handleNavigationOnClick);

        b.placeName.setText(place.getName());
        b.placeAddress.setText(place.getLocation().address);
        b.placeDescription.setText(place.getDescription());

        photoAdapter = new PhotoAdapter<>(requireContext(), place.getImages());
        b.viewPager.setAdapter(photoAdapter);
        b.circleIndicator.setViewPager(b.viewPager);
        photoAdapter.registerAdapterDataObserver(b.circleIndicator.getAdapterDataObserver());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
        amenities = place.getAmenities();
        amenityAdapter = new AmenityAdapter(requireContext(), amenities);
        b.amenities.setLayoutManager(linearLayoutManager);
        b.amenities.setAdapter(amenityAdapter);

        b.chatBtn.setOnClickListener(v -> {
            // Lead to chat
        });
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