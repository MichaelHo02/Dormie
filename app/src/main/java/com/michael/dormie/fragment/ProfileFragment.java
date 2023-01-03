package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.michael.dormie.R;
import com.michael.dormie.activity.SignInActivity;
import com.michael.dormie.recyclerview.ProfileAdapter;
import com.michael.dormie.recyclerview.ProfileCard;
import com.michael.dormie.utils.NavigationUtil;

import java.util.ArrayList;
import java.util.List;

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

    private MaterialToolbar topAppBar;
    private ImageView profileImage;
    private TextView cardTitle, cardContent;

    private View view;
    private RecyclerView recyclerView;
    private List<ProfileCard> profileCardList;
    private ProfileAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbRef;

    private String userId, email;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
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
        initUI();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        dbRef.addValueEventListener(new ValueEventListener() {
            String title, content;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    title = userSnapshot.getKey();
                    content = userSnapshot.child(title).getValue(String.class);
                    break;
                }
//                cardTitle.setText(title);
//                cardContent.setText(content);
                profileCardList.add(new ProfileCard(title, content));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        adapter = new ProfileAdapter(profileCardList);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        return view;
    }

    private void initUI() {
        topAppBar = view.findViewById(R.id.fragment_profile_top_bar);
        topAppBar.setNavigationOnClickListener(this::handleNavigationOnClick);

        profileImage = view.findViewById(R.id.profile_image);
        profileImage.setOnClickListener(this::handleSignOutClick);

        recyclerView = view.findViewById(R.id.rcv_profile);

        cardTitle = view.findViewById(R.id.profile_card_title);
        cardContent = view.findViewById(R.id.profile_card_content);


    }

    private void handleSignOutClick(View view) {
        NavigationUtil.navigateActivity(ProfileFragment.this, ProfileFragment.this.getContext(), SignInActivity.class, 10);
    }

    private void handleNavigationOnClick(View view) {
    }
}