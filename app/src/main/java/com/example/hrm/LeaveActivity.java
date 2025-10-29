package com.example.hrm;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LeaveActivity extends AppCompatActivity {

    private Spinner leaveTypeSpinner;
    private Button startDateButton, endDateButton, submitLeaveButton;
    private EditText reasonEditText;
    private RecyclerView leaveHistoryRecyclerView;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String selectedStartDate = "", selectedEndDate = "";

    private String userId, userToken;
    private LeaveAdapter leaveAdapter;
    private ArrayList<JSONObject> leaveList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave);

        // 🔹 Get data from login intent
        userId = getIntent().getStringExtra("USER_ID");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        leaveTypeSpinner = findViewById(R.id.leaveTypeSpinner);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        reasonEditText = findViewById(R.id.reasonEditText);
        submitLeaveButton = findViewById(R.id.submitLeaveButton);
        leaveHistoryRecyclerView = findViewById(R.id.leaveHistoryRecyclerView);

        // 🔹 Leave type options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.leave_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaveTypeSpinner.setAdapter(adapter);

        // 🔹 RecyclerView setup
        leaveAdapter = new LeaveAdapter(leaveList);
        leaveHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaveHistoryRecyclerView.setAdapter(leaveAdapter);

        // 🔹 Date pickers
        startDateButton.setOnClickListener(v -> showDatePicker(true));
        endDateButton.setOnClickListener(v -> showDatePicker(false));

        // 🔹 Submit leave button
        submitLeaveButton.setOnClickListener(v -> submitLeaveRequest());

        // 🔹 Load previous leaves
        fetchLeaveHistory();
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;
        new DatePickerDialog(
                LeaveActivity.this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String date = dateFormat.format(calendar.getTime());
                    if (isStartDate) {
                        selectedStartDate = date;
                        startDateButton.setText("Start: " + date);
                    } else {
                        selectedEndDate = date;
                        endDateButton.setText("End: " + date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ✅ Submit a leave request
    private void submitLeaveRequest() {
        String reason = reasonEditText.getText().toString().trim();
        String leaveType = leaveTypeSpinner.getSelectedItem().toString();

        if (selectedStartDate.isEmpty() || selectedEndDate.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("start_date", selectedStartDate);
            jsonBody.put("end_date", selectedEndDate);
            jsonBody.put("type", leaveType);
            jsonBody.put("reason", reason);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("LeaveActivity", "Submitting leave: " + jsonBody);

        // ✅ Use service role key to bypass RLS
        SupabaseHelper.postWithServiceRole("leaves", jsonBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LeaveActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d("LeaveActivity", "Leave response: " + responseBody);

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(LeaveActivity.this, "Leave submitted successfully", Toast.LENGTH_SHORT).show();
                        reasonEditText.setText("");
                        startDateButton.setText("Select Start Date");
                        endDateButton.setText("Select End Date");
                        selectedStartDate = "";
                        selectedEndDate = "";
                        fetchLeaveHistory();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(LeaveActivity.this,
                                    "Failed to submit request. Code: " + response.code(),
                                    Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    // ✅ Fetch all previous leave requests for the current user
    private void fetchLeaveHistory() {
        String query = "user_id=eq." + userId + "&order=start_date.desc";

        SupabaseHelper.get("leaves", query, null, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LeaveActivity.this, "Error loading leaves: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "[]";
                Log.d("LeaveActivity", "Leave history: " + responseBody);

                try {
                    JSONArray jsonArray = new JSONArray(responseBody);
                    leaveList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        leaveList.add(jsonArray.getJSONObject(i));
                    }
                    runOnUiThread(() -> leaveAdapter.notifyDataSetChanged());
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(LeaveActivity.this, "Error parsing leave data", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
