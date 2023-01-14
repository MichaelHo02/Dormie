package com.michael.dormie;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.michael.dormie.databinding.FragmentHomeTenantBinding;
import com.michael.dormie.model.Place;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.utils.FireBaseDBPath;

import java.util.ArrayList;
import java.util.List;

public class HomeTenantFragment extends Fragment {
    private static final String TAG = "HomeTenantFragment";

    FragmentHomeTenantBinding b;
    Tenant tenantReference;

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

        fetchData();
    }

    private void fetchData() {
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
                    tenantReference = documentSnapshot.toObject(Tenant.class);

                    final GeoLocation center = new GeoLocation(
                            tenantReference.getSchool().lat,
                            tenantReference.getSchool().lng);
                    final double radiusInM = tenantReference.getMaxDistance() * 1000;

                    List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
                    final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                    for (GeoQueryBounds b : bounds) {
                        Query q = db.collection(FireBaseDBPath.PROPERTIES)
                                .orderBy("geoHash")
                                .startAt(b.startHash)
                                .endAt(b.endHash);
                        tasks.add(q.get());
                    }

                    Tasks.whenAllComplete(tasks)
                            .addOnCompleteListener(t -> {
                                List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                                for (Task<QuerySnapshot> task : tasks) {
                                    QuerySnapshot snap = task.getResult();
                                    for (DocumentSnapshot doc : snap.getDocuments()) {
                                        Place place = doc.toObject(Place.class);
                                        double lat = place.getLocation().lat;
                                        double lng = place.getLocation().lng;

                                        GeoLocation docLocation = new GeoLocation(lat, lng);
                                        double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                        if (distanceInM <= radiusInM) matchingDocs.add(doc);
                                    }
                                }

                                // TODO add to adapter
                            });
                });
    }
}