package com.example.hrm;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapViewActivity";
    private GoogleMap mMap;
    private String userId, userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userId = getIntent().getStringExtra("USER_ID");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (userId != null && userToken != null) {
            fetchLocationHistory();
        } else {
            Toast.makeText(this, "User ID or Token is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchLocationHistory() {
        // Fetch locations for a specific user, ordered by time
        String query = "user_id=eq." + userId + "&select=*&order=updated_at.asc";
        
        SupabaseHelper.get("live_location", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch location history", e);
                runOnUiThread(() -> Toast.makeText(MapViewActivity.this, "Failed to load route", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Failed response from Supabase: " + response.code());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);
                    
                    List<LatLng> routePoints = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject point = jsonArray.getJSONObject(i);
                        double lat = point.getDouble("latitude");
                        double lng = point.getDouble("longitude");
                        routePoints.add(new LatLng(lat, lng));
                    }

                    if (!routePoints.isEmpty()) {
                        runOnUiThread(() -> drawPolyline(routePoints));
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing location history", e);
                }
            }
        });
    }

    private void drawPolyline(List<LatLng> routePoints) {
        if (mMap == null || routePoints.size() < 2) {
            return; // Not enough points to draw a line
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .color(Color.BLUE)
                .width(10);
        
        mMap.addPolyline(polylineOptions);

        // Create bounds that include all points of the route
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : routePoints) {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();

        // Move the camera to show the entire route with some padding
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
