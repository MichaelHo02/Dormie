package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michael.dormie.R;
import com.michael.dormie.activity.PostCreationActivity;
import com.michael.dormie.activity.SignInActivity;
import com.michael.dormie.adapter.PlaceAdapter;
import com.michael.dormie.model.Place;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.SignalCode;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;
    private MaterialToolbar topAppBar;
    private RecyclerView recyclerView;
    private FloatingActionButton addBtn;
    private List<Place> places;
    private PlaceAdapter placeAdapter;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private FirebaseAuth mAuth;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this.requireActivity(), gso);

        initUI();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.e("Current User", currentUser.getEmail());
        }
    }

    private void initUI() {
        topAppBar = view.findViewById(R.id.fragment_home_top_bar);
        recyclerView = view.findViewById(R.id.fragment_home_rv);
        addBtn = view.findViewById(R.id.fragment_home_float_action);
        topAppBar.setNavigationOnClickListener(this::handleNavigationOnClick);
        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.profile_page) {
                    gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            NavigationUtil.navigateActivity(
                                    HomeFragment.this,
                                    HomeFragment.this.getContext(),
                                    SignInActivity.class,
                                    10);
                        }
                    });
                }
                return false;
            }
        });
        recycleViewInit();
        addBtn.setOnClickListener(this::handleBAddBtnOnClick);
    }

    private void handleBAddBtnOnClick(View view) {
        Bundle bundle = new Bundle();
        NavigationUtil.navigateActivity(this, this.requireContext(), PostCreationActivity.class, SignalCode.TEMPLATE_FORMAT, bundle);
    }

    private void handleNavigationOnClick(View view) {
        DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.activity_master_drawer_layout);
        drawerLayout.open();
    }


    private void recycleViewInit() {
        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(manager);
        places = getPlaceList();
        placeAdapter = new PlaceAdapter(places);
        recyclerView.setAdapter(placeAdapter);
    }

    private List<Place> getPlaceList() {
        List<Place> places = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            places.add(new Place());
        }
        return places;
    }
}