package com.example.hrm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EmployeeActivity extends AppCompatActivity {

    private String userId, userToken;
    private TextView shiftDetailsTextView, shiftTitleTextView;
    private ImageView refreshShiftButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        userId = getIntent().getStringExtra("USER_ID");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        // Initialize views from the included shift card
        shiftTitleTextView = findViewById(R.id.shiftTitleTextView);
        shiftDetailsTextView = findViewById(R.id.shiftDetailsTextView);
        refreshShiftButton = findViewById(R.id.refreshShiftButton);

        // Initialize dashboard card views
        CardView profileCard = findViewById(R.id.profileCard);
        CardView leaveCard = findViewById(R.id.leaveCard);
        CardView payslipCard = findViewById(R.id.payslipCard);
        CardView attendanceCard = findViewById(R.id.attendanceCard);
        CardView overtimeCard = findViewById(R.id.overtimeCard);
        Button logoutButton = findViewById(R.id.logoutButton);

        // Set click listeners for dashboard cards
        profileCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_TOKEN", userToken);
            startActivity(intent);
        });

        leaveCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, LeaveActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_TOKEN", userToken);
            startActivity(intent);
        });

        payslipCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, PayslipActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_TOKEN", userToken);
            startActivity(intent);
        });

        attendanceCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AttendanceActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_TOKEN", userToken);
            intent.putExtra("USER_ROLE", "employee");
            startActivity(intent);
        });

        overtimeCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, OvertimeActivity.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_TOKEN", userToken);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Set listener for the refresh button
        refreshShiftButton.setOnClickListener(v -> fetchShiftForTomorrow());

        // Fetch today's shift when the activity is first created
        fetchShiftForToday();
    }

    private void fetchShiftForToday() {
        shiftTitleTextView.setText("Today's Shift");
        fetchShiftForDate(new Date());
    }

    private void fetchShiftForTomorrow() {
        shiftTitleTextView.setText("Tomorrow's Shift");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        fetchShiftForDate(calendar.getTime());
    }

    private void fetchShiftForDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateStr = dateFormat.format(date);

        String query = "user_id=eq." + userId + "&date=eq." + dateStr + "&select=shifts(start_time,end_time)";

        runOnUiThread(() -> shiftDetailsTextView.setText("Loading..."));

        SupabaseHelper.get("shift_assignments", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> shiftDetailsTextView.setText("Not Assigned"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> shiftDetailsTextView.setText("Not Assigned"));
                    return;
                }
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    if (jsonArray.length() > 0) {
                        JSONObject assignment = jsonArray.getJSONObject(0);
                        JSONObject shift = assignment.getJSONObject("shifts");
                        String startTime = shift.getString("start_time");
                        String endTime = shift.getString("end_time");
                        final String shiftText = startTime + " - " + endTime;
                        runOnUiThread(() -> shiftDetailsTextView.setText(shiftText));
                    } else {
                        runOnUiThread(() -> shiftDetailsTextView.setText("Not Assigned"));
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> shiftDetailsTextView.setText("Error"));
                }
            }
        });
    }
}
