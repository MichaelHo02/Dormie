package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.michael.dormie.R;
import com.michael.dormie.activity.MasterActivity;
import com.michael.dormie.activity.SignInActivity;
import com.michael.dormie.recyclerview.ProfileAdapter;
import com.michael.dormie.recyclerview.ProfileCard;
import com.michael.dormie.utils.NavigationUtil;

import java.util.ArrayList;

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

    private DatabaseReference mDatabase;
    private DatabaseReference mUserReference;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    private View view;
    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private ArrayList<ProfileCard> profileCards;
    private MaterialToolbar topAppBar;

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

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_setting, container, false);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this.requireActivity(), gso);

        return view;
    }

    private void initUI() {
        topAppBar = view.findViewById(R.id.fragment_profile_top_bar);
        topAppBar.setNavigationOnClickListener(this::handleProfileNavigationClick);
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.profile_page) {
                    gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            NavigationUtil.navigateActivity(
                                    ProfileFragment.this,
                                    ProfileFragment.this.getContext(),
                                    SignInActivity.class,
                                    10);
                        }
                    });
                }
                return false;
            }
        });

        // Recyclerview setup
        recyclerView = view.findViewById(R.id.rcv_profile);
        profileCards = getUserInfo(mUser);
        adapter = new ProfileAdapter(profileCards);
        recyclerView.setAdapter(adapter);
    }

    private void handleProfileNavigationClick(View view) {
    }

    private ArrayList<ProfileCard> getUserInfo(FirebaseUser user) {
        ArrayList<ProfileCard> cards = new ArrayList<>();

        String userId = user.getUid();

        DocumentReference documentReference = db.collection("users").document(userId);

        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                cards.add(new ProfileCard("Date of birth", value.getString("dob")));
                cards.add(new ProfileCard("Role", value.getString("role")));
            }
        });

        return cards;
    }

}
