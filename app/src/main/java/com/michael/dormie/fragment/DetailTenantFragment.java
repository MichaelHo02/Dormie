package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;
import com.michael.dormie.adapter.AmenityAdapter;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.databinding.FragmentDetailTenantBinding;
import com.michael.dormie.model.Place;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.model.User;

import java.util.List;

public class DetailTenantFragment extends Fragment {

    FragmentDetailTenantBinding b;

    private PhotoAdapter<String> photoAdapter;
    private List<String> photos;
    private AmenityAdapter amenityAdapter;
    private List<String> amenities;
    private DocumentReference doc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentDetailTenantBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Place place = DetailTenantFragmentArgs.fromBundle(getArguments()).getPlace();
        Tenant tenant = DetailTenantFragmentArgs.fromBundle(getArguments()).getTenant();

        doc = FirebaseFirestore.getInstance().collection("users").document(place.getAuthorId());
        doc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot != null) {
                User user = documentSnapshot.toObject(User.class);
                Glide.with(b.getRoot()).load(user.getAvatar()).into(b.avatarImageView);
                b.lessorName.setText(user.getName());
                b.lessorEmail.setText(user.getEmail());
            }
        });

        b.topAppBar.setNavigationOnClickListener(this::handleNavigationOnClick);
        b.topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.map) {
                    navigateToMapActivity(tenant.getSchool(), place.getLocation());
                    return true;
                }
                return false;
            }
        });

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

        b.chatBtn.setOnClickListener(v -> {
            // Lead to chat
        });
    }


    private void navigateToMapActivity(Tenant.Location tenant, Place.Location place) {
        DetailTenantFragmentDirections.ActionTenantDetailFragmentToMapsActivity directions =
                DetailTenantFragmentDirections.actionTenantDetailFragmentToMapsActivity(tenant, place);
        Navigation.findNavController(getView()).navigate(directions);
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