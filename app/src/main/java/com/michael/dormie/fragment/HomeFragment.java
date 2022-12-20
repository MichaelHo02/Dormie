package com.michael.dormie.fragment;

import android.os.Bundle;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;
import com.michael.dormie.R;
import com.michael.dormie.adapter.PlaceAdapter;
import com.michael.dormie.model.Place;

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
    private List<Place> places;
    private PlaceAdapter placeAdapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        initUI();
        return view;
    }

    private void initUI() {
        topAppBar = view.findViewById(R.id.fragment_home_top_bar);
        recyclerView = view.findViewById(R.id.fragment_home_rv);
        topAppBar.setNavigationOnClickListener(this::handleNavigationOnClick);
        recycleViewInit();

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