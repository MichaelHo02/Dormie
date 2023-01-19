package com.michael.dormie.activity;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_MAGENTA;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ROSE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.michael.dormie.R;
import com.michael.dormie.databinding.ActivityMapTenantBinding;
import com.michael.dormie.model.Tenant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapTenantActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapTenantActivity";

    private GoogleMap mMap;
    private ActivityMapTenantBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMapTenantBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(b.map.getId());
        mapFragment.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        Tenant.Location tenant = MapTenantActivityArgs.fromBundle(bundle).getTenant();
        com.michael.dormie.model.Place.Location place = MapTenantActivityArgs.fromBundle(bundle).getPlace();

        if (tenant != null && place != null) {
            b.placeCard.setVisibility(View.VISIBLE);
            b.originName.setText(tenant.name);
            b.originAddress.setText(tenant.address);
            b.desName.setText(place.name);
            b.desAddress.setText(place.address);
        }

        b.topAppBar.setNavigationOnClickListener(this::handleNavigationOnClick);

    }

    private void handleNavigationOnClick(View view) {
        Navigation.findNavController(b.getRoot()).navigate(MapTenantActivity);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        Bundle bundle = getIntent().getExtras();
        Tenant.Location tenant = MapTenantActivityArgs.fromBundle(bundle).getTenant();
        com.michael.dormie.model.Place.Location place = MapTenantActivityArgs.fromBundle(bundle).getPlace();
        if (tenant != null && place != null) {
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
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        JSONArray routes = response.getJSONArray("routes");
                        ArrayList<LatLng> points;
                        PolylineOptions polyline = null;

                        String distance = routes.getJSONObject(0).getJSONArray("legs")
                                .getJSONObject(0).getJSONObject("distance").getString("text");

                        String duration = routes.getJSONObject(0).getJSONArray("legs")
                                .getJSONObject(0).getJSONObject("duration").getString("text");
                        b.distanceDuration.setText(duration + " (" + distance + ")");
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
                        mMap.addMarker(new MarkerOptions()
                                .position(originLatLng).title("School"));

                        mMap.addMarker(new MarkerOptions()
                                        .position(desLatLng).title("Rental Location"));

                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(originLatLng)
                                .include(desLatLng)
                                .build();
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, point.x, 150, 20));
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