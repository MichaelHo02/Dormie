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

    private static final String TAG = "MapsActivity";
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;


    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private SearchBar searchBar;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private LocationAdapter locationAdapter;
    private LinearLayout cardView;
    private MaterialTextView placeName, placeAddress;
    private MaterialButton submitButton;

    private final Map<String, List<AutocompletePrediction>> cache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        locationAdapter = new LocationAdapter(new ArrayList<>(), (pos, autocompletePrediction) -> {
            searchBar.setText(autocompletePrediction.getPrimaryText(null));
            searchView.hide();

            // Define a Place ID.
            final String placeId = autocompletePrediction.getPlaceId();
            // Specify the fields to return.
            final List<Place.Field> placeFields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
            // Construct a request object, passing the place ID and fields array.
            final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);
            if (!Places.isInitialized()) {
                // Initialize the SDK
//                Places.initialize(getApplicationContext(), getString(R.string.google_api_key));
                Places.initialize(getApplicationContext(), "AIzaSyA3KCcs8KYT8fcMaAUwgSTE3SpeIXGb5Sw");
            }
            // Create a new PlacesClient instance
            PlacesClient placesClient = Places.createClient(this);

            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                Log.i(TAG, "Place found: " + place.getName());

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(autocompletePrediction.getPrimaryText(null).toString()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));

                cardView.setVisibility(View.VISIBLE);
                String name = autocompletePrediction.getPrimaryText(null).toString();
                String address = autocompletePrediction.getSecondaryText(null).toString();
                placeName.setText(name);
                placeAddress.setText(address);
                submitButton.setOnClickListener(view -> {
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
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(locationAdapter);

        searchBar = findViewById(R.id.search_bar);
        searchView = findViewById(R.id.search_view);
        searchView.getEditText().addTextChangedListener(
                new PlaceSearchingWatcher(this, searchView.getEditText(), cache, locationAdapter));

        cardView = findViewById(R.id.place_card);
        placeName = findViewById(R.id.place_name);
        placeAddress = findViewById(R.id.place_address);
        submitButton = findViewById(R.id.submit_button);

        searchBar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}