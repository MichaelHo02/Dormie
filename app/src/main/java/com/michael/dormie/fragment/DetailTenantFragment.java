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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.michael.dormie.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.michael.dormie.adapter.AmenityAdapter;
import com.michael.dormie.adapter.PhotoAdapter;
import com.michael.dormie.databinding.FragmentDetailTenantBinding;
import com.michael.dormie.model.ChatRoom;
import com.michael.dormie.model.Place;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.model.User;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DetailTenantFragment extends Fragment {

    FragmentDetailTenantBinding b;

    private DocumentReference doc;
    private Place place;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentDetailTenantBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        place = DetailTenantFragmentArgs.fromBundle(getArguments()).getPlace();
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
                    navigateToMapActivity(tenant, place);
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
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(FireBaseDBPath.CHAT_ROOM)
                    .whereArrayContains("userIds", currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                            createChatRoom();
                            return;
                        }

                        List<ChatRoom> chatRooms = queryDocumentSnapshots.toObjects(ChatRoom.class);
                        for (ChatRoom chatRoom : chatRooms) {
                            if (chatRoom.getUserIds().containsAll(Arrays.asList(currentUser.getUid()
                                    , place.getAuthorId()))) {
                                Navigation.findNavController(b.getRoot()).navigate(
                                        DetailTenantFragmentDirections.actionTenantDetailFragmentToChatFragment());
                                return;
                            }
                        }
                        createChatRoom();
                    });

        });
    }

    private void createChatRoom() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = UUID.randomUUID().toString();
        ChatRoom chatRoom = new ChatRoom(uid, Arrays.asList(currentUser.getUid(), place.getAuthorId()));
        db.collection(FireBaseDBPath.CHAT_ROOM)
                .document(uid)
                .set(chatRoom, SetOptions.merge())
                .addOnSuccessListener(t -> {
                    Navigation.findNavController(b.getRoot()).navigate(
                            DetailTenantFragmentDirections.actionTenantDetailFragmentToChatFragment());
                });
    }

    private void navigateToMapActivity(Tenant tenant, Place place) {
        DetailTenantFragmentDirections.ActionTenantDetailFragmentToMapTenantActivity directions =
                DetailTenantFragmentDirections.actionTenantDetailFragmentToMapTenantActivity(tenant, place);
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