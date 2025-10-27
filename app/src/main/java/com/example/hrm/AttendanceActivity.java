package com.example.hrm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AttendanceActivity extends AppCompatActivity {

    private Button punchInButton, punchOutButton;
    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter adapter;
    private List<DailyAttendance> dailyAttendanceList = new ArrayList<>();

    private String userId, userToken, userRole;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userId = getIntent().getStringExtra("USER_ID");
        userToken = getIntent().getStringExtra("USER_TOKEN");
        userRole = getIntent().getStringExtra("USER_ROLE");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        punchInButton = findViewById(R.id.punchInButton);
        punchOutButton = findViewById(R.id.punchOutButton);
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);

        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(dailyAttendanceList);
        attendanceRecyclerView.setAdapter(adapter);

        punchInButton.setOnClickListener(v -> {
            handlePunch("punch_in");
            Intent serviceIntent = new Intent(this, LocationUpdateService.class);
            serviceIntent.putExtra("USER_ID", userId);
            serviceIntent.putExtra("USER_TOKEN", userToken);
            startService(serviceIntent);
        });

        punchOutButton.setOnClickListener(v -> {
            handlePunch("punch_out");
            stopService(new Intent(this, LocationUpdateService.class));
        });

        fetchAttendanceHistory();
    }

    private void handlePunch(String punchType) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocationAndPunch(punchType);
        }
    }

    private void getCurrentLocationAndPunch(String punchType) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { return; }

        runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "Fetching current location...", Toast.LENGTH_SHORT).show());

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        saveAttendanceRecord(punchType, location.getLatitude(), location.getLongitude());
                    } else {
                        runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "Failed to get location. Please ensure location is enabled and try again.", Toast.LENGTH_LONG).show());
                    }
                })
                .addOnFailureListener(this, e -> {
                    runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                });
    }

    private void saveAttendanceRecord(String punchType, Double latitude, Double longitude) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("type", punchType);
            if (latitude != null && longitude != null) {
                jsonBody.put("latitude", latitude);
                jsonBody.put("longitude", longitude);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SupabaseHelper.post("attendance", userToken, jsonBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "Request failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(AttendanceActivity.this, "Attendance marked successfully", Toast.LENGTH_SHORT).show();
                        fetchAttendanceHistory();
                    });
                } else {
                    final String errorBody = response.body().string();
                    runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "Failed to mark attendance: " + errorBody, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void fetchAttendanceHistory() {
        String query = "user_id=eq." + userId + "&select=*&order=created_at.asc";
        SupabaseHelper.get("attendance", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "Failed to fetch history", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    final String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);
                    processAttendanceData(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void processAttendanceData(JSONArray rawData) throws JSONException {
        Map<String, List<Date>> punchesByDay = new LinkedHashMap<>();
        for (int i = 0; i < rawData.length(); i++) {
            JSONObject record = rawData.getJSONObject(i);
            String timestamp = record.getString("created_at");
            Date date = parseDate(timestamp);
            if (date != null) {
                String dayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
                if (!punchesByDay.containsKey(dayKey)) {
                    punchesByDay.put(dayKey, new ArrayList<>());
                }
                punchesByDay.get(dayKey).add(date);
            }
        }

        dailyAttendanceList.clear();
        for (Map.Entry<String, List<Date>> entry : punchesByDay.entrySet()) {
            String dayKey = entry.getKey();
            List<Date> punches = entry.getValue();
            Collections.sort(punches);

            String punchInTime = "-";
            String punchOutTime = "-";

            if (!punches.isEmpty()) {
                punchInTime = new SimpleDateFormat("hh:mm a", Locale.US).format(punches.get(0));
                if (punches.size() > 1) {
                    punchOutTime = new SimpleDateFormat("hh:mm a", Locale.US).format(punches.get(punches.size() - 1));
                }
            }
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dayKey);
                String displayDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date);
                dailyAttendanceList.add(new DailyAttendance(displayDate, punchInTime, punchOutTime));
            } catch (ParseException e) {
                // This should not happen as we are controlling the format
            }
        }
        Collections.reverse(dailyAttendanceList); // Show most recent days first

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private Date parseDate(String timestamp) {
        try {
            // Handle different timestamp formats from Supabase
            if (timestamp.contains("+")) {
                int dotIndex = timestamp.lastIndexOf('.');
                int plusIndex = timestamp.lastIndexOf('+');
                if (dotIndex != -1 && plusIndex > dotIndex) {
                    timestamp = timestamp.substring(0, dotIndex) + timestamp.substring(plusIndex);
                }
            }
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return inputFormat.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Please punch in/out again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_LONG).show();
            }
        }
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
