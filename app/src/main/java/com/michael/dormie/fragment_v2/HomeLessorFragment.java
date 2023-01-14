package com.michael.dormie.fragment_v2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.michael.dormie.R;
import com.michael.dormie.adapter.PlaceAdapter;
import com.michael.dormie.databinding.ActivityMainLessorBinding;
import com.michael.dormie.databinding.FragmentHomeLessorBinding;
import com.michael.dormie.model.Place;

import java.util.ArrayList;
import java.util.List;

public class HomeLessorFragment extends Fragment {
    FragmentHomeLessorBinding b;
    private List<Place> places;
    private PlaceAdapter placeAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentHomeLessorBinding.inflate(inflater, container, false);
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
        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        b.recycleView.setLayoutManager(manager);
        places = getPlaceList();
        placeAdapter = new PlaceAdapter(places);
        b.recycleView.setAdapter(placeAdapter);

        b.toolbar.setNavigationOnClickListener(v -> {
            DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
            drawerLayout.open();
        });
        b.fab.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(HomeLessorFragmentDirections.actionHomeLessorFragmentToPlaceCreationFragment());
        });

        b.bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.home_bottom_search) {

                }
                return false;
            }
        });
    }

    private List<Place> getPlaceList() {
        List<Place> places = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            places.add(new Place());
        }
        return places;
    }
}