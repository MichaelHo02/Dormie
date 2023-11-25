package com.michael.dormie.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;
import com.michael.dormie.databinding.FragmentProfileBinding;
import com.michael.dormie.model.User;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    FragmentProfileBinding b;
    private GoogleSignInClient gsc;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentProfileBinding.inflate(inflater, container, false);
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
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this.requireActivity(), gso);
        b.toolbar.setNavigationOnClickListener(v -> {
            DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
            drawerLayout.open();
        });

        b.signOutBtn.setOnClickListener(this::handleSignOutClick);
        b.delAccBtn.setOnClickListener(this::handleDeleteAccClick);
    }

    private void handleSignOutClick(View view) {
        auth.signOut();
        gsc.signOut().addOnSuccessListener(task -> {
            Navigation.findNavController(b.getRoot()).popBackStack();
            this.requireActivity().finish();
        });
    }

    private void handleDeleteAccClick(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        user.delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "User account deleted.");
                    Navigation.findNavController(b.getRoot()).popBackStack();
                    this.requireActivity().finish();
                    Toast.makeText(this.getContext(), "Successfully delete the account!",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Could not delete the account"));
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Could not the user.");
            return;
        }
        b.profileEmail.setText(currentUser.getEmail());
        b.profileName.setText(currentUser.getDisplayName());
        Glide.with(b.getRoot()).load(currentUser.getPhotoUrl()).into(b.avatarImageView);
        DocumentReference doc = db.collection("users").document(currentUser.getUid());
        doc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot != null) {
                User user = documentSnapshot.toObject(User.class);
                b.profileDOB.setText(user.getDob());
                b.profileRole.setText(user.getRole());
            }
        });
    }
}
