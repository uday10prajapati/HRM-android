package com.example.hrm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private RecyclerView assignCallRecyclerView;
    private AssignCallAdapter assignCallAdapter;
    private final List<AssignCall> assignCalls = new ArrayList<>();

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

        assignCallRecyclerView = findViewById(R.id.assignCallRecyclerView);
        assignCallRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignCallAdapter = new AssignCallAdapter(assignCalls, this);
        assignCallRecyclerView.setAdapter(assignCallAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAssignedCalls();
    }

    private void fetchAssignedCalls() {
        String query = "select=*"; // Fetch all calls

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

                    List<AssignCall> tempCalls = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject callObject = jsonArray.getJSONObject(i);
                        
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
                        tempCalls.add(assignCall);

                    }

                    runOnUiThread(() -> {
                        assignCalls.clear();
                        assignCalls.addAll(tempCalls);
                        assignCallAdapter.notifyDataSetChanged();
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
