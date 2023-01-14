package com.michael.dormie.fragment_v2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.michael.dormie.R;
import com.michael.dormie.adapter.PlaceAdapter;
import com.michael.dormie.databinding.ActivityMainLessorBinding;
import com.michael.dormie.databinding.FragmentHomeLessorBinding;
import com.michael.dormie.model.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycleViewInit();

        b.toolbar.setNavigationOnClickListener(v -> {
            DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
            drawerLayout.open();
        });
        b.fab.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(HomeLessorFragmentDirections.actionHomeLessorFragmentToPlaceCreationFragment());
        });

        SearchView searchView = (SearchView) b.bottomAppBar.getMenu().findItem(R.id.home_bottom_search).getActionView();
        searchView.setQueryHint("Search place name here...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String searchText = newText.toLowerCase();
                List<Place> temp = new ArrayList<>();

                for (Place place : places) {
                    if (place.name.toLowerCase(Locale.ROOT).contains(searchText)) {
                        temp.add(place);
                    }
                }

                if (temp.isEmpty()) {
                    Toast.makeText(getContext(), "No places found.", Toast.LENGTH_SHORT).show();
                } else {
                    placeAdapter.setFilteredList(temp);
                }

                return false;
            }
        });
    }

    private void recycleViewInit() {
        places = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("properties")
                .whereEqualTo("authorId", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Place place = document.toObject(Place.class);
                                places.add(place);
                            }

                            LinearLayoutManager manager = new LinearLayoutManager(requireContext());
                            b.recycleView.setLayoutManager(manager);
                            placeAdapter = new PlaceAdapter(getContext(), places);
                            b.recycleView.setAdapter(placeAdapter);
                        } else {
                            System.out.println("Error");
                        }

                    }
                });

    }
}