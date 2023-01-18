package com.michael.dormie.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.michael.dormie.R;
import com.michael.dormie.adapter.LocationAdapter;
import com.michael.dormie.databinding.ActivityMapsBinding;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.utils.PlaceSearchingWatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final String PARAM_LOCATION_NAME = "name";
    public static final String PARAM_LOCATION_ADDRESS = "address";
    public static final String PARAM_LOCATION_LAT_LNG = "latLng";
    public static final String PARAM_LOCATION_TYPE_FILTER = "filter";

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private ActivityMapsBinding b;

    private final Map<String, List<AutocompletePrediction>> cache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(b.map.getId());
        mapFragment.getMapAsync(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        // TODO: Handle error with given status code.
        LocationAdapter locationAdapter = new LocationAdapter(new ArrayList<>(), (pos, autocompletePrediction) -> {
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        Bundle bundle = getIntent().getExtras();
        Tenant.Location tenant = MapsActivityArgs.fromBundle(bundle).getTenantLocation();
        com.michael.dormie.model.Place.Location place = MapsActivityArgs.fromBundle(bundle).getPlaceLocation();
        if (tenant != null) {
            LatLng originLatLng = new LatLng(tenant.lat, tenant.lng);
            LatLng desLatLng = new LatLng(place.lat, place.lng);
            direction(originLatLng, desLatLng);
        }
    }

    private void direction(LatLng originLatLng, LatLng desLatLng) {
        String originLLng = originLatLng.latitude + ", "  + originLatLng.longitude;
        String desLLng = desLatLng.latitude + ", "  + desLatLng.longitude;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json?")
                .buildUpon()
                .appendQueryParameter("origin", originLLng)
                .appendQueryParameter("destination", desLLng)
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", getString(R.string.google_api_key))
                .toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e(TAG, "HELLO");
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        JSONArray routes = response.getJSONArray("routes");
                        ArrayList<LatLng> points;
                        PolylineOptions polyline = null;

                        for (int r = 0; r < routes.length(); r++) {
                            points = new ArrayList<>();
                            polyline = new PolylineOptions();
                            JSONArray legs = routes.getJSONObject(r).getJSONArray("legs");
                            
                            for (int l = 0; l < legs.length(); l++) {
                                JSONArray steps = legs.getJSONObject(l).getJSONArray("steps");
                                for (int s = 0; s < steps.length(); s++) {
                                    String line = steps.getJSONObject(s).getJSONObject("polyline").getString("points");
                                    List<LatLng> latLngs = decodePoly(line);
                                    for (int t = 0; t < latLngs.size(); t++) {
                                        LatLng position = new LatLng((latLngs.get(t)).latitude, (latLngs.get(t)).longitude);
                                        points.add(position);
                                    }
                                }
                            }
                            polyline.addAll(points);
                            polyline.width(10);
                            polyline.color(Color.BLUE);
                            polyline.geodesic(true);
                        }

                        mMap.addPolyline(polyline);
                        mMap.addMarker(new MarkerOptions().position(originLatLng).title("1"));
                        mMap.addMarker(new MarkerOptions().position(desLatLng).title("2"));

                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(originLatLng)
                                .include(desLatLng)
                                .build();
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, point.x, 200, 20));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, String.valueOf(error));
            }
        });
        RetryPolicy retryPolicy = new DefaultRetryPolicy(400000000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((double) lat /1E5),
                    ((double) lng/1E5));
            poly.add(p);
        }
        return poly;
    }
}