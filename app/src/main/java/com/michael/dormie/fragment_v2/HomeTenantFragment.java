package com.michael.dormie.fragment_v2;

import android.os.Bundle;
import android.os.Handler;
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
import androidx.recyclerview.widget.RecyclerView;

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
import com.michael.dormie.implement.PaginationScrollingListener;
import com.michael.dormie.model.Place;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeTenantFragment extends Fragment {
    private static final String TAG = "HomeTenantFragment";
    private final int MAX_REQUEST = 1;

    private FragmentHomeTenantBinding b;
    private List<Place> places;
    private PlaceAdapter placeAdapter;
    private LinearLayoutManager manager;
    private Tenant tenantReference;
    private List<Query> queries;

    private boolean isLoading;
    private boolean isLastPage;
    private int totalPage = 2;
    private int currentPage = 1;

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
        queries = new ArrayList<>();

        b.toolbar.setNavigationOnClickListener(this::handleNavigationOnClick);
        b.toolbar.setOnMenuItemClickListener(this::handleMenuOnClick);

        manager = new LinearLayoutManager(requireContext());
        placeAdapter = new PlaceAdapter(this.requireContext(), places, place -> Navigation
                .findNavController(b.getRoot()).navigate(HomeTenantFragmentDirections.actionHomeTenantFragmentToTenantDetailFragment(place, tenantReference)));
        b.recycleView.setLayoutManager(manager);
        b.recycleView.setHasFixedSize(true);
        b.recycleView.setAdapter(placeAdapter);
        b.recycleView.addOnScrollListener(createInfiniteScrollListener());

        fetchInitData(() -> {
            placeAdapter.setFilteredList(places);
            if (currentPage < totalPage) {
                placeAdapter.addFooterLoading();
            } else {
                isLastPage = true;
            }
        });
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

    private RecyclerView.OnScrollListener createInfiniteScrollListener() {
        return new PaginationScrollingListener(manager) {
            @Override
            public void onLoadItem() {
                isLoading = true;
                currentPage++;
                new Handler().postDelayed(() -> fetchData(() -> {
                    placeAdapter.removeFooterLoading();
                    placeAdapter.setFilteredList(places);
                    isLoading = false;
                    if (currentPage < totalPage) {
                        placeAdapter.addFooterLoading();
                    } else {
                        isLastPage = true;
                    }
                }), 2000);
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }
        };
    }

    private void fetchInitData(ICallBack callBack) {
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
                    Log.e(TAG, "A");
                    Log.e(TAG, documentSnapshot.getData().toString());
                    tenantReference = documentSnapshot.toObject(Tenant.class);

                    final GeoLocation center = new GeoLocation(
                            tenantReference.getSchool().lat,
                            tenantReference.getSchool().lng);

                    List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center,
                            tenantReference.getMaxDistance());
                    final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                    for (GeoQueryBounds b : bounds) {
                        Log.e(TAG, "B");
                        Query q = db.collection(FireBaseDBPath.PROPERTIES)
                                .orderBy("location.geoHash")
                                .startAt(b.startHash)
                                .endAt(b.endHash)
                                .limit(MAX_REQUEST);
                        tasks.add(q.get());
                    }
                    executeTasks(tasks, center, callBack);
                });
    }

    private void fetchData(ICallBack callBack) {
        List<Task<QuerySnapshot>> tasks = new ArrayList();
        for (Query query : queries) {
            tasks.add(query.get());
        }
        final GeoLocation center = new GeoLocation(
                tenantReference.getSchool().lat,
                tenantReference.getSchool().lng);
        executeTasks(tasks, center, callBack);
    }

    private void executeTasks(List<Task<QuerySnapshot>> tasks, GeoLocation center, ICallBack callBack) {
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(t -> {
                    Log.e(TAG, "C");
                    for (Task<QuerySnapshot> task : tasks) {
                        QuerySnapshot snap = task.getResult();
                        Log.e(TAG, "D");
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            Log.e(TAG, "E");
                            if (!doc.exists()) continue;
                            Place place = doc.toObject(Place.class);
                            double lat = place.getLocation().lat;
                            double lng = place.getLocation().lng;

                            GeoLocation docLocation = new GeoLocation(lat, lng);
                            double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                            if (distanceInM <= tenantReference.getMaxDistance()) {
                                Log.e(TAG, "F");
                                places.add(place);
                            }
                        }
                    }
                    callBack.onCallback();
                });
    }
}