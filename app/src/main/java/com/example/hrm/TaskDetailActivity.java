package com.example.hrm;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView title, description, customerName, customerAddress, customerMobile, dueDate;
    private Spinner statusSpinner;
    private Button updateStatusButton;
    private EditText notesEditText;

    private Task task;
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        task = (Task) getIntent().getSerializableExtra("TASK");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        title = findViewById(R.id.titleTextView);
        description = findViewById(R.id.descriptionTextView);
        customerName = findViewById(R.id.customerNameTextView);
        customerAddress = findViewById(R.id.customerAddressTextView);
        customerMobile = findViewById(R.id.customerMobileTextView);
        dueDate = findViewById(R.id.dueDateTextView);
        statusSpinner = findViewById(R.id.statusSpinner);
        updateStatusButton = findViewById(R.id.updateStatusButton);
        notesEditText = findViewById(R.id.notesEditText);

        // Populate the UI with the task details
        title.setText(task.getTitle());
        description.setText(task.getDescription());
        customerName.setText(task.getCustomerName());
        customerAddress.setText(task.getCustomerAddress());
        customerMobile.setText(task.getCustomerMobile());
        dueDate.setText(task.getDueDate());

        // Setup the status spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.task_statuses, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        updateStatusButton.setOnClickListener(v -> updateStatus());
    }

    private void updateStatus() {
        String newStatus = statusSpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("status", newStatus);
            jsonBody.put("completion_notes", notes);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // This assumes you want to update the 'tasks' table.
        // If you have a separate 'service_calls' table, change "tasks" to "service_calls"
        String query = "id=eq." + task.getId();
        SupabaseHelper.patch("tasks", query, userToken, jsonBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(TaskDetailActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(TaskDetailActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to the list
                    });
                } else {
                    final String errorBody = response.body().string();
                    runOnUiThread(() -> Toast.makeText(TaskDetailActivity.this, "Failed to update status: " + errorBody, Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}
