package com.michael.dormie;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.michael.dormie.adapter.PlaceAdapter;
import com.michael.dormie.databinding.FragmentHomeTenantBinding;
import com.michael.dormie.implement.ICallBack;
import com.michael.dormie.implement.PaginationScrollingListener;
import com.michael.dormie.model.Place;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeTenantFragment extends Fragment {
    private static final String TAG = "HomeTenantFragment";
    private final int MAX_REQUEST = 1;

    FragmentHomeTenantBinding b;
    private Set<Place> places;
    private PlaceAdapter placeAdapter;
    private LinearLayoutManager manager;
    private Tenant tenantReference;
    private List<Query> queries;

    private boolean isLoading;
    private boolean isLastPage;
    private int totalPage = 4;
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
        places = new HashSet<>();
        queries = new ArrayList<>();

        b.toolbar.setNavigationOnClickListener(v -> {
            DrawerLayout drawerLayout = view.getRootView().findViewById(R.id.drawerLayout);
            drawerLayout.open();
        });

        manager = new LinearLayoutManager(requireContext());
        int size = places.size();
        placeAdapter = new PlaceAdapter(this.requireContext(),
                Arrays.asList(places.toArray(new Place[size])));
        b.recycleView.setLayoutManager(manager);
        b.recycleView.setHasFixedSize(true);
        b.recycleView.setAdapter(placeAdapter);
        b.recycleView.addOnScrollListener(createInfiniteScrollListener());

        fetchInitData(() -> {
            int tmpSize = places.size();
            placeAdapter.setFilteredList(Arrays.asList(places.toArray(new Place[tmpSize])));
            if (currentPage < totalPage) {
                placeAdapter.addFooterLoading();
            } else {
                isLastPage = true;
            }
        });
    }

    private RecyclerView.OnScrollListener createInfiniteScrollListener() {
        return new PaginationScrollingListener(manager) {
            @Override
            public void onLoadItem() {
                isLoading = true;
                currentPage++;
                new Handler().postDelayed(() -> fetchData(() -> {
                    placeAdapter.removeFooterLoading();
                    int tmpSize = places.size();
                    placeAdapter.setFilteredList(Arrays.asList(places.toArray(new Place[tmpSize])));
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
                        DocumentSnapshot lastDoc = null;
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
                                lastDoc = doc;
                                places.add(place);
                            }
                        }
                        Query query = snap.getQuery();
                        int idx = queries.indexOf(query);
                        if (lastDoc != null) query.startAfter(lastDoc);
                        if (idx == -1) {
                            queries.add(query);
                        } else {
                            queries.add(idx, query);
                        }
                    }
                    callBack.onCallback();
                });
    }
}