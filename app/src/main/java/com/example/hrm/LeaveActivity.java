package com.example.hrm;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LeaveActivity extends AppCompatActivity {

    private Button startDateButton, endDateButton, submitLeaveButton;
    private Spinner leaveTypeSpinner;
    private EditText reasonEditText;
    private RecyclerView leaveHistoryRecyclerView;
    private LeaveAdapter adapter;
    private List<Leave> leaveHistory = new ArrayList<>();

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private String userId, userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userId = getIntent().getStringExtra("USER_ID");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        submitLeaveButton = findViewById(R.id.submitLeaveButton);
        leaveTypeSpinner = findViewById(R.id.leaveTypeSpinner);
        reasonEditText = findViewById(R.id.reasonEditText);
        leaveHistoryRecyclerView = findViewById(R.id.leaveHistoryRecyclerView);

        // Setup Spinner for leave types
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.leave_types, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaveTypeSpinner.setAdapter(spinnerAdapter);

        // Setup RecyclerView
        leaveHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaveAdapter(leaveHistory);
        leaveHistoryRecyclerView.setAdapter(adapter);

        // Set button listeners
        startDateButton.setOnClickListener(v -> showDatePickerDialog(startCalendar, startDateButton));
        endDateButton.setOnClickListener(v -> showDatePickerDialog(endCalendar, endDateButton));
        submitLeaveButton.setOnClickListener(v -> submitLeaveRequest());

        fetchLeaveHistory();
    }

    private void showDatePickerDialog(Calendar calendar, Button button) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            button.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void submitLeaveRequest() {
        String startDate = dateFormat.format(startCalendar.getTime());
        String endDate = dateFormat.format(endCalendar.getTime());
        String leaveType = leaveTypeSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("start_date", startDate);
            jsonBody.put("end_date", endDate);
            jsonBody.put("type", leaveType);
            jsonBody.put("reason", reason);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SupabaseHelper.post("leaves", userToken, jsonBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(LeaveActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(LeaveActivity.this, "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
                        fetchLeaveHistory(); // Refresh the history
                    });
                } else {
                    final String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    runOnUiThread(() -> Toast.makeText(LeaveActivity.this, "Failed to submit request. Code: " + response.code(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void fetchLeaveHistory() {
        String query = "select=*";
        SupabaseHelper.get("leaves", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(LeaveActivity.this, "Failed to fetch leave history: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    final String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    runOnUiThread(() -> Toast.makeText(LeaveActivity.this, "Error fetching leaves: " + errorBody, Toast.LENGTH_LONG).show());
                    return;
                }
                try {
                    final String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);
                    if (jsonArray.length() == 0) {
                        runOnUiThread(() -> Toast.makeText(LeaveActivity.this, "Connected to database, but no leave history found. Please check Row Level Security (RLS) in your Supabase project.", Toast.LENGTH_LONG).show());
                        return;
                    }
                    leaveHistory.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject leaveObject = jsonArray.getJSONObject(i);
                        leaveHistory.add(new Leave(
                                leaveObject.optString("type", "N/A"),
                                leaveObject.optString("start_date", "N/A"),
                                leaveObject.optString("end_date", "N/A"),
                                leaveObject.optString("status", "N/A"),
                                leaveObject.optString("reason", "N/A")
                        ));
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(LeaveActivity.this, "Error parsing leave history.", Toast.LENGTH_SHORT).show());
                }
            }
        });
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
