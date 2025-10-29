package com.example.hrm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AssignCallActivity extends AppCompatActivity implements AssignCallAdapter.OnItemClickListener {

    private static final String TAG = "AssignCallActivity";

    private RecyclerView pendingCallsRecyclerView, resolvedCallsRecyclerView;
    private AssignCallAdapter pendingAdapter, resolvedAdapter;
    private final List<AssignCall> pendingCalls = new ArrayList<>();
    private final List<AssignCall> resolvedCalls = new ArrayList<>();

    private TextView pendingCountTextView, resolvedCountTextView;
    private String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_call);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userToken = getIntent().getStringExtra("USER_TOKEN");

        pendingCountTextView = findViewById(R.id.pendingCountTextView);
        resolvedCountTextView = findViewById(R.id.resolvedCountTextView);

        pendingCallsRecyclerView = findViewById(R.id.pendingCallsRecyclerView);
        pendingCallsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        pendingAdapter = new AssignCallAdapter(pendingCalls, this);
        pendingCallsRecyclerView.setAdapter(pendingAdapter);

        resolvedCallsRecyclerView = findViewById(R.id.resolvedCallsRecyclerView);
        resolvedCallsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resolvedAdapter = new AssignCallAdapter(resolvedCalls, this);
        resolvedCallsRecyclerView.setAdapter(resolvedAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAssignedCalls();
    }

    private void fetchAssignedCalls() {
        String query = "role=ilike.engineer";

        SupabaseHelper.get("assign_call", query, userToken, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch calls", e);
                runOnUiThread(() -> Toast.makeText(AssignCallActivity.this, "Failed to fetch calls", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error fetching calls. Code: " + response.code() + ", Body: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(AssignCallActivity.this, "Error fetching calls: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    JSONArray jsonArray = new JSONArray(responseBody);
                    Log.d(TAG, "Fetched " + jsonArray.length() + " calls.");

                    List<AssignCall> tempPending = new ArrayList<>();
                    List<AssignCall> tempResolved = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject callObject = jsonArray.getJSONObject(i);
                        
                        // **THE FIX**: Use 'optLong' and 'optString' for all fields to prevent crashes
                        // if a value is missing or null in the database.
                        AssignCall assignCall = new AssignCall(
                                callObject.optLong("call_id"),
                                callObject.optString("id"),
                                callObject.optString("created_at"),
                                callObject.optString("name"),
                                callObject.optString("role"),
                                callObject.optLong("mobile_number"),
                                callObject.optString("dairy_name"),
                                callObject.optString("problem"),
                                callObject.optString("description"),
                                callObject.optString("status")
                        );

                        if ("pending".equalsIgnoreCase(assignCall.getStatus())) {
                            tempPending.add(assignCall);
                        } else {
                            tempResolved.add(assignCall);
                        }
                    }

                    runOnUiThread(() -> {
                        pendingCalls.clear();
                        pendingCalls.addAll(tempPending);
                        pendingAdapter.notifyDataSetChanged();
                        pendingCountTextView.setText("(" + pendingCalls.size() + ")");

                        resolvedCalls.clear();
                        resolvedCalls.addAll(tempResolved);
                        resolvedAdapter.notifyDataSetChanged();
                        resolvedCountTextView.setText("(" + resolvedCalls.size() + ")");
                    });

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing call data: " + responseBody, e);
                    runOnUiThread(() -> Toast.makeText(AssignCallActivity.this, "Error parsing call data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void onItemClick(AssignCall call) {
        Intent intent = new Intent(this, AssignCallDetailActivity.class);
        intent.putExtra("ASSIGNED_CALL", call);
        intent.putExtra("USER_TOKEN", userToken);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
