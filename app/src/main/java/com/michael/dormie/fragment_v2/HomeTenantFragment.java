package com.michael.dormie.fragment_v2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.michael.dormie.R;
import com.michael.dormie.adapter.PlaceAdapter;
import com.michael.dormie.databinding.FragmentHomeTenantBinding;
import com.michael.dormie.implement.ICallBack;
import com.michael.dormie.model.Place;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeTenantFragment extends Fragment {
    private static final String TAG = "HomeTenantFragment";

    private FragmentHomeTenantBinding b;
    private List<Place> places;
    private PlaceAdapter placeAdapter;
    private Tenant tenantReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentHomeTenantBinding.inflate(inflater, container, false);
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
        places = new ArrayList<>();

        b.toolbar.setNavigationOnClickListener(this::handleNavigationOnClick);
        b.toolbar.setOnMenuItemClickListener(this::handleMenuOnClick);

        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        placeAdapter = new PlaceAdapter(this.requireContext(), places, place -> Navigation
                .findNavController(b.getRoot()).navigate(
                        HomeTenantFragmentDirections.actionHomeTenantFragmentToTenantDetailFragment(place, tenantReference)));
        b.recycleView.setLayoutManager(manager);
        b.recycleView.setHasFixedSize(true);
        b.recycleView.setAdapter(placeAdapter);

        b.refreshLayout.setOnRefreshListener(() -> {
            fetchData(() -> placeAdapter.setFilteredList(places));
            b.refreshLayout.setRefreshing(false);
        });
        fetchData(() -> placeAdapter.setFilteredList(places));
    }

    private void handleNavigationOnClick(View view) {
        DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
        drawerLayout.open();
    }

    private boolean handleMenuOnClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.home_top_search) {
            SearchView searchView = (SearchView) b.toolbar.getMenu().findItem(R.id.home_top_search).getActionView();
            searchView.setQueryHint("Search place name here");
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
                        if (place.getName().toLowerCase(Locale.ROOT).contains(searchText)) {
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
        return false;
    }

    private void fetchData(ICallBack callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "There is no user");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FireBaseDBPath.TENANTS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;
                    Log.e(TAG, documentSnapshot.getData().toString());
                    tenantReference = documentSnapshot.toObject(Tenant.class);

                    final GeoLocation center = new GeoLocation(
                            tenantReference.getSchool().lat,
                            tenantReference.getSchool().lng);

                    List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center,
                            tenantReference.getMaxDistance());
                    final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                    for (GeoQueryBounds b : bounds) {
                        Query q = db.collection(FireBaseDBPath.PROPERTIES)
                                .orderBy("location.geoHash")
                                .startAt(b.startHash)
                                .endAt(b.endHash);
                        tasks.add(q.get());
                    }
                    executeTasks(tasks, center, callBack);
                });
    }

    private void executeTasks(List<Task<QuerySnapshot>> tasks, GeoLocation center, ICallBack callBack) {
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(t -> {
                    for (Task<QuerySnapshot> task : tasks) {
                        QuerySnapshot snap = task.getResult();
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            if (!doc.exists()) continue;
                            Place place = doc.toObject(Place.class);
                            double lat = place.getLocation().lat;
                            double lng = place.getLocation().lng;

                            GeoLocation docLocation = new GeoLocation(lat, lng);
                            double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                            if (distanceInM <= tenantReference.getMaxDistance() && !places.contains(place)) {
                                places.add(place);
                            }
                        }
                    }
                    callBack.onCallback();
                });
    }
}