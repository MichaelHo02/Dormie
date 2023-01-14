package com.michael.dormie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.michael.dormie.R;
import com.michael.dormie.adapter.LocationAdapter;
import com.michael.dormie.databinding.ActivityHostBinding;
import com.michael.dormie.databinding.ActivityMapsBinding;
import com.michael.dormie.implement.IClickableCallback;
import com.michael.dormie.utils.PlaceSearchingWatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final String PARAM_LOCATION_NAME = "name";
    public static final String PARAM_LOCATION_ADDRESS = "address";
    public static final String PARAM_LOCATION_LAT_LNG = "latLng";
    public static final String PARAM_LOCATION_TYPE_FILTER = "filter";

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private LocationAdapter locationAdapter;
    private ActivityMapsBinding b;

    private final Map<String, List<AutocompletePrediction>> cache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(b.map.getId());
        mapFragment.getMapAsync(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        locationAdapter = new LocationAdapter(new ArrayList<>(), (pos, autocompletePrediction) -> {
            b.searchBar.setText(autocompletePrediction.getPrimaryText(null));
            b.searchView.hide();

            final String placeId = autocompletePrediction.getPlaceId();
            final List<Place.Field> placeFields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
            final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
            }
            PlacesClient placesClient = Places.createClient(this);
            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                Log.i(TAG, "Place found: " + place.getName());

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(autocompletePrediction.getPrimaryText(null).toString()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));

                b.placeCard.setVisibility(View.VISIBLE);
                String name = autocompletePrediction.getPrimaryText(null).toString();
                String address = autocompletePrediction.getSecondaryText(null).toString();
                b.placeName.setText(name);
                b.placeAddress.setText(address);
                b.submitBtn.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.putExtra(PARAM_LOCATION_NAME, name);
                    intent.putExtra(PARAM_LOCATION_ADDRESS, address);
                    intent.putExtra(PARAM_LOCATION_LAT_LNG, place.getLatLng());
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();
                    // TODO: Handle error with given status code.
                    Intent intent = new Intent();
                    intent.putExtra(PARAM_LOCATION_NAME, "Max Quota");
                    intent.putExtra(PARAM_LOCATION_ADDRESS, "This is a response from Max Quota");
                    intent.putExtra(PARAM_LOCATION_LAT_LNG, new LatLng(12, 12));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        });
        b.recycleView.setLayoutManager(linearLayoutManager);
        b.recycleView.setAdapter(locationAdapter);

        Intent it = getIntent();
        List<String> filters = it.getExtras().getStringArrayList(PARAM_LOCATION_TYPE_FILTER);
        b.searchView.getEditText().addTextChangedListener(
                new PlaceSearchingWatcher(
                        this, b.searchView.getEditText(), cache, locationAdapter, filters));
        b.searchBar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}