package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

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
import com.michael.dormie.utils.NavigationUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "ProfileFragment";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    private View view;
    private MaterialToolbar topAppBar;
    private ShapeableImageView avatar;
    private TextView name, email, dob, role;
    private MaterialButton deleteBtn, signOutBtn;

    public ProfileFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this.requireActivity(), gso);

        initUI();
        return view;
    }

    private void initUI() {
        topAppBar = view.findViewById(R.id.fragment_profile_top_bar);
        avatar = view.findViewById(R.id.pf_avatar);
        name = view.findViewById(R.id.pf_name);
        email = view.findViewById(R.id.pf_email);
        dob = view.findViewById(R.id.pf_dob);
        role = view.findViewById(R.id.pf_role);

        topAppBar.setNavigationOnClickListener(this::handleNavigationOnclick);

        // Navigate Button
        deleteBtn = view.findViewById(R.id.delete_acc_btn);
        signOutBtn = view.findViewById(R.id.sign_out_btn);
        deleteBtn.setOnClickListener(this::handleDeleteAccClick);
        signOutBtn.setOnClickListener(this::handleSignOutClick);
    }

    private void handleNavigationOnclick(View view) {
        DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.activity_master_drawer_layout);
        drawerLayout.open();
    }

    private void handleSignOutClick(View view) {
        auth.signOut();
        gsc.signOut().addOnCompleteListener(task -> {
            ProfileFragment.this.requireActivity().finish();
            NavigationUtil.navigateActivity(requireActivity(), ProfileFragment.this.requireContext(),
                    SignInActivity.class, 20);
            Toast.makeText(ProfileFragment.this.getContext(), "Successfully log out!", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleDeleteAccClick(View view) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User account deleted.");
                NavigationUtil.navigateActivity(requireActivity(), ProfileFragment.this.getContext(), SignInActivity.class, 20);
                Toast.makeText(ProfileFragment.this.getContext(), "Successfully delete the account!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Could not delete the account");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Could not the user.");
            return;
        }

        email.setText(user.getEmail());
        name.setText(user.getDisplayName());
        Glide.with(view).load(user.getPhotoUrl()).into(avatar);
        DocumentReference doc = db.collection("users").document(user.getUid());
        doc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot != null) {
                dob.setText(documentSnapshot.getString("dob"));
                role.setText(documentSnapshot.getString("role"));
            }
        });
    }
}
