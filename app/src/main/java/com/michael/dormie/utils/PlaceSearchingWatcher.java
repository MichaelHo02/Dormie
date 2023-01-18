package com.michael.dormie.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.michael.dormie.R;
import com.michael.dormie.adapter.LocationAdapter;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlaceSearchingWatcher implements TextWatcher {
    private static final String TAG = "PlaceSearchingWatcher";

    private Context context;
    private final EditText editText;
    private Map<String, List<AutocompletePrediction>> cache;
    private LocationAdapter locationAdapter;
    private List<String> typesFilter;

    public PlaceSearchingWatcher(Context context, EditText editText, Map<String,
            List<AutocompletePrediction>> cache, LocationAdapter locationAdapter, List<String> typesFilter) {
        this.context = context;
        this.editText = editText;
        this.cache = cache;
        this.locationAdapter = locationAdapter;
        this.typesFilter = typesFilter;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String s = editText.getText().toString();
        if (s.isEmpty()) return;
        if (s.length() < 2) return;

        if (cache.containsKey(s)) {
            locationAdapter.setData(cache.get(s));
            return;
        }

        Log.d(TAG, "Query the prediction");
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(R.string.google_api_key), Locale.getDefault());
        }
        PlacesClient placesClient = Places.createClient(context);

        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setCountries("VN")
                .setTypesFilter(typesFilter)
                .setQuery(editText.getText().toString())
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            cache.put(s, response.getAutocompletePredictions());
            locationAdapter.setData(response.getAutocompletePredictions());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
    }
}
