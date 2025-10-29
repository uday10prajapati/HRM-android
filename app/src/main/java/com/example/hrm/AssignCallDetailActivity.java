package com.example.hrm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AssignCallDetailActivity extends AppCompatActivity {

    private AssignCall assignCall;
    private EditText solutionEditText;
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_call_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        assignCall = getIntent().getParcelableExtra("ASSIGNED_CALL");
        userToken = getIntent().getStringExtra("USER_TOKEN");

        if (assignCall == null) {
            Toast.makeText(this, "Error: Call data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView dairyNameTextView = findViewById(R.id.dairyNameTextView);
        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView mobileNumberTextView = findViewById(R.id.mobileNumberTextView);
        TextView problemTextView = findViewById(R.id.problemTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        solutionEditText = findViewById(R.id.solutionEditText);
        Button resolveButton = findViewById(R.id.resolveButton);

        dairyNameTextView.setText(assignCall.getDairyName());
        nameTextView.setText("Contact: " + assignCall.getName());
        mobileNumberTextView.setText("Mobile: " + assignCall.getMobileNumber());
        problemTextView.setText(assignCall.getProblem());
        descriptionTextView.setText(assignCall.getDescription());

        if ("pending".equalsIgnoreCase(assignCall.getStatus())) {
            solutionEditText.setVisibility(View.VISIBLE);
            resolveButton.setVisibility(View.VISIBLE);
            resolveButton.setOnClickListener(v -> resolveCall());
        } else {
            solutionEditText.setVisibility(View.GONE);
            resolveButton.setVisibility(View.GONE);
        }
    }

    private void resolveCall() {
        String solution = solutionEditText.getText().toString().trim();
        if (solution.isEmpty()) {
            Toast.makeText(this, "Please enter a solution", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("status", "resolved");
            jsonBody.put("description", assignCall.getDescription() + "\n\nSolution: " + solution);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // **THE FIX**: Use the unique 'call_id' to guarantee the correct call is updated.
        String query = "call_id=eq." + assignCall.getCallId();
        SupabaseHelper.patch("assign_call", query, userToken, jsonBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(AssignCallDetailActivity.this, "Failed to resolve call", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(AssignCallDetailActivity.this, "Call resolved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(AssignCallDetailActivity.this, "Error resolving call", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
