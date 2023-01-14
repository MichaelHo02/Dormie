package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;
import com.michael.dormie.activity.SignInActivity;
import com.michael.dormie.databinding.FragmentHomeLessorBinding;
import com.michael.dormie.databinding.FragmentProfileBinding;
import com.michael.dormie.utils.NavigationUtil;

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

        initEditFormBtn();
        b.signOutBtn.setOnClickListener(this::handleSignOutClick);
        b.delAccBtn.setOnClickListener(this::handleDeleteAccClick);
        b.editFormBtn.setOnClickListener(this::handleEditFormClick);
    }

    private void initEditFormBtn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().get("role").equals("lessor")) {
                    b.editFormBtn.setVisibility(View.INVISIBLE);
                } else {
                    b.editFormBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void handleEditFormClick(View view) {
        Navigation.findNavController(b.getRoot()).navigate(ProfileFragmentDirections.actionProfileFragmentToEditFormFragment());
    }

    private void handleSignOutClick(View view) {
        auth.signOut();
        gsc.signOut().addOnSuccessListener(task -> {
            this.requireActivity().finish();
            Toast.makeText(ProfileFragment.this.getContext(), "Successfully log out!", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleDeleteAccClick(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        user.delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "User account deleted.");
                    this.requireActivity().finish();
                    Toast.makeText(ProfileFragment.this.getContext(), "Successfully delete the account!", Toast.LENGTH_SHORT).show();
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
        //Glide.with(b.getRoot()).load(currentUser.getPhotoUrl()).into(b.avatarImageView);
        DocumentReference doc = db.collection("users").document(currentUser.getUid());
        doc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot != null) {
                b.profileDOB.setText(documentSnapshot.getString("dob"));
                b.profileRole.setText(documentSnapshot.getString("role"));
            }
        });
    }
}
