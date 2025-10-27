package com.example.hrm;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.MarkerOptions;
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

    private GoogleMap mMap;
    private String userId, userToken, date;

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
        date = getIntent().getStringExtra("DATE");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        fetchLocationHistory();
    }

    private void fetchLocationHistory() {
        // The query needs to filter by both user_id and the specific date.
        // The date format must match what is stored in your database.
        String query = "user_id=eq." + userId + "&timestamp=gte." + date + "T00:00:00.000Z&timestamp=lt." + date + "T23:59:59.999Z&select=*";

        SupabaseHelper.get("location_history", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MapViewActivity.this, "Failed to fetch location history.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MapViewActivity.this, "No location history found for this day.", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    if (jsonArray.length() < 2) {
                        runOnUiThread(() -> Toast.makeText(MapViewActivity.this, "Not enough data to draw a path.", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    List<LatLng> pathPoints = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject point = jsonArray.getJSONObject(i);
                        pathPoints.add(new LatLng(point.getDouble("latitude"), point.getDouble("longitude")));
                    }

                    runOnUiThread(() -> drawPathOnMap(pathPoints));

                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(MapViewActivity.this, "Failed to parse location data.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void drawPathOnMap(List<LatLng> pathPoints) {
        if (mMap == null || pathPoints.isEmpty()) return;

        LatLng startPoint = pathPoints.get(0);
        LatLng endPoint = pathPoints.get(pathPoints.size() - 1);

        mMap.addMarker(new MarkerOptions().position(startPoint).title("Punch In"));
        mMap.addMarker(new MarkerOptions().position(endPoint).title("Punch Out"));

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(pathPoints)
                .color(Color.BLUE)
                .width(10);
        mMap.addPolyline(polylineOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : pathPoints) {
            builder.include(point);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
